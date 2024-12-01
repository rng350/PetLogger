package com.hfad.petlogger.photos.usecases

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.tags.Tag
import java.time.OffsetDateTime

class BuildPhotoSearchQueryUseCase(
    private val photosAmt: Int,
    private val pickFrom: Pick? = null
) {
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))
    operator fun invoke(
        query: String,
        lastPhotoDate: OffsetDateTime,
        lastPhotoId: Long,
        photoIdSelectionPool: List<Long>? = null
    ): SimpleSQLiteQuery? {
        val parsedSearch = parseSearchQuery(query)
        // get bounding dates
        val getBoundingSearchDates = GetBoundingSearchDatesUseCase()
        val startAndEndDates = getBoundingSearchDates(parsedSearch["before"]?:listOf(), parsedSearch["after"]?:listOf())
        if (startAndEndDates == GetBoundingSearchDatesUseCase.Result.Invalid) return null

        val nonCategorizedSearch = parsedSearch[null]?:listOf()

        val searchedPets =
            if (pickFrom is Pick.FromPet && pickFrom.petName.isNotEmpty()) {
                val petsHashedSet = parsedSearch["pet"]?.toHashSet() ?: HashSet<String>()
                petsHashedSet.add(pickFrom.petName)
                petsHashedSet.toList()
            } else parsedSearch["pet"]?.toHashSet()?.toList()?:listOf()
        val searchedTags =
            if (pickFrom is Pick.FromTag && pickFrom.tagName.isNotEmpty()) {
                val tagsHashedSet = parsedSearch["#"]?.toHashSet() ?: HashSet<String>()
                tagsHashedSet.add(pickFrom.tagName)
                tagsHashedSet.toList()
            } else parsedSearch["#"]?.toHashSet()?.toList()?:listOf()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        queryBuilder.append("SELECT photo_table.* FROM photo_table ")
        if (pickFrom is Pick.FromNote) {
            queryBuilder.append("JOIN photo_note_table ON photo_note_table.photo_id=photo_table.photo_id ")
        }
        else if (pickFrom is Pick.FromEvent) {
            queryBuilder.append("JOIN photo_event_table ON photo_event_table.photo_id=photo_table.photo_id ")
        }

        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("JOIN pet_photo_table ON pet_photo_table.photo_id=photo_table.photo_id ")
            queryBuilder.append("JOIN pet_table ON pet_photo_table.pet_id=pet_table.pet_id ")
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("JOIN photo_tag_table ON photo_tag_table.photo_id=photo_table.photo_id ")
            queryBuilder.append("JOIN tag_table ON photo_tag_table.tag_id=tag_table.tag_id ")
        }

        queryBuilder.append("WHERE (datetime(photo_table.photo_date), photo_table.photo_id) < (datetime(?), ?) ")
        queryParams.add("${Converter.fromOffsetDateTime(lastPhotoDate)}")
        queryParams.add(lastPhotoId)

        when(startAndEndDates) {
            is GetBoundingSearchDatesUseCase.Result.BoundingEndSearchDate -> {
                queryBuilder.append("AND datetime(photo_table.photo_date) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingSearchDates -> {
                queryBuilder.append("AND datetime(photo_table.photo_date) > datetime(?) ")
                queryBuilder.append("AND datetime(photo_table.photo_date) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingStartSearchDate -> {
                queryBuilder.append("AND datetime(photo_table.photo_date) > datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
            }
            else -> {}
        }

        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("AND pet_table.pet_name IN ${searchedPets.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedPets)
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("AND tag_table.tag_name IN ${searchedTags.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedTags)
        }

        if (pickFrom is Pick.FromNote) {
            queryBuilder.append("AND photo_note_table.note_id = ? ")
            queryParams.add(pickFrom.noteId)
        }
        else if (pickFrom is Pick.FromEvent) {
            queryBuilder.append("AND photo_event_table.event_id = ? ")
            queryParams.add(pickFrom.eventId)
        }

        photoIdSelectionPool?.let {
            if (photoIdSelectionPool.isEmpty()) {
                return null
            }
            queryBuilder.append("AND photo_table.photo_id IN ${photoIdSelectionPool.joinToString(prefix="(",separator=",",postfix=")"){"?"}} ")
            queryParams.addAll(photoIdSelectionPool)
        }

        queryBuilder.append("GROUP BY photo_table.photo_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty()) {
            havingCountQuery.append("HAVING COUNT(DISTINCT pet_photo_table.pet_id) = ? ")
            queryParams.add(searchedPets.size)
        }
        if (searchedTags.isNotEmpty()) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT photo_tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size)
        }
        queryBuilder.append(havingCountQuery)

        queryBuilder.append("ORDER BY datetime(photo_table.photo_date) DESC, photo_table.photo_id DESC LIMIT ?")
        queryParams.add(photosAmt)

        Log.d("PhotoQuery", "query: $queryBuilder")
        Log.d("PhotoQuery", "Params: ${queryParams.map{it.toString()}}")

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }
    sealed class Pick {
        data class FromPet(val pet: LiveData<Pet>): Pick() {
            val petName: String get() = pet.value?.petName ?: ""
        }
        data class FromNote(val noteId: Long): Pick()
        data class FromEvent(val eventId: Long): Pick()
        data class FromTag(val tag: LiveData<Tag>): Pick() {
            val tagName: String get() = tag.value?.tagName ?: ""
        }
    }
}