package com.hfad.petlogger.photos.domain.usecases

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.util.Converter
import java.time.OffsetDateTime

class BuildPhotoSearchQueryUseCase(
    private val get: Get = Get.Photo,
    private val photosAmt: Int? = null,
    private val pickFrom: Pick? = null
) {
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))
    operator fun invoke(
        query: String,
        lastPhotoDate: OffsetDateTime = Constants.OFFSET_DATE_TIME_MAX_ALLOWED,
        lastPhotoId: Long = Long.MAX_VALUE
    ): SimpleSQLiteQuery? {
        val parsedSearch = parseSearchQuery(query)
        // get bounding dates
        val getBoundingSearchDates = GetBoundingSearchDatesUseCase()
        val startAndEndDates = getBoundingSearchDates(parsedSearch["before"]?:listOf(), parsedSearch["after"]?:listOf())
        if (startAndEndDates == GetBoundingSearchDatesUseCase.Result.Invalid) return null

        val nonCategorizedSearch = parsedSearch[null]?:listOf()

        val searchedPets = parsedSearch["pet"]?.toHashSet()?.toList()?:listOf()
        val searchedTags = parsedSearch["#"]?.toHashSet()?.toList()?:listOf()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        when (pickFrom) {
            is Pick.FromEvent -> {
                queryBuilder.append("WITH photos_of_given_event AS (SELECT photo_id FROM photo_event_table WHERE event_id = ?) ")
                queryParams.add(pickFrom.eventId)
            }
            is Pick.FromNote -> {
                queryBuilder.append("WITH photos_of_given_note AS (SELECT photo_id FROM photo_note_table WHERE note_id = ?) ")
                queryParams.add(pickFrom.noteId)
            }
            is Pick.FromPet -> {
                queryBuilder.append("WITH photos_of_given_pet AS (SELECT photo_id FROM pet_photo_table WHERE pet_id = ?) ")
                queryParams.add(pickFrom.petId)
            }
            is Pick.FromTag -> {
                queryBuilder.append("WITH photos_of_given_tag AS (SELECT photo_id FROM photo_tag_table WHERE tag_id = ?) ")
                queryParams.add(pickFrom.tagId)
            }
            null -> {}
        }

        when (get) {
            Get.OnlyIds -> {
                queryBuilder.append("SELECT photo_table.photo_id ")
            }
            Get.Photo -> {
                queryBuilder.append("SELECT photo_table.* ")
            }
        }
        queryBuilder.append("FROM photo_table ")

        when (pickFrom) {
            is Pick.FromEvent -> {
                queryBuilder.append("JOIN photos_of_given_event ON photo_table.photo_id = photos_of_given_event.photo_id ")
            }
            is Pick.FromNote -> {
                queryBuilder.append("JOIN photos_of_given_note ON photo_table.photo_id = photos_of_given_note.photo_id ")
            }
            is Pick.FromPet -> {
                queryBuilder.append("JOIN photos_of_given_pet ON photo_table.photo_id = photos_of_given_pet.photo_id ")
            }
            is Pick.FromTag -> {
                queryBuilder.append("JOIN photos_of_given_tag ON photo_table.photo_id = photos_of_given_tag.photo_id ")
            }
            null -> {}
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

        queryBuilder.append("GROUP BY photo_table.photo_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty()) {
            havingCountQuery.append("HAVING COUNT(DISTINCT pet_table.pet_name) = ? ")
            queryParams.add(searchedPets.size)
        }
        if (searchedTags.isNotEmpty()) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT photo_tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size)
        }
        queryBuilder.append(havingCountQuery)

        queryBuilder.append("ORDER BY datetime(photo_table.photo_date) DESC, photo_table.photo_id DESC ")
        photosAmt?.let {
            queryBuilder.append("LIMIT ? ")
            queryParams.add(photosAmt)
        }

        Log.d("PhotoQuery", "query: $queryBuilder")
        Log.d("PhotoQuery", "Params: ${queryParams.map{it.toString()}}")

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }
    sealed class Pick {
        data class FromPet(val petId: Long): Pick()
        data class FromNote(val noteId: Long): Pick()
        data class FromEvent(val eventId: Long): Pick()
        data class FromTag(val tagId: Long): Pick()
    }

    sealed class Get {
        data object OnlyIds: Get()
        data object Photo: Get()
    }
}