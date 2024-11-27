package com.hfad.petlogger.notes.usecases

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteDao
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.photos.PhotoDao
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagDao
import com.hfad.petlogger.weights.WeightDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class GetMoreOfSearchedNotesUseCase(
    private val noteDao: NoteDao,
    private val notesAmt: Int,
    private val pickFrom: PickFrom? = null
    ): GetSearchedItemsUseCase<Note> {
    override var currentQuery: String = ""
    private var lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))

    override suspend fun invoke(): List<Note> = withContext(Dispatchers.IO) {
        val parsedSearch = parseSearchQuery(currentQuery)
        // get bounding dates
        val getBoundingSearchDates = GetBoundingSearchDatesUseCase()
        val startAndEndDates = getBoundingSearchDates(parsedSearch["before"]?:listOf(), parsedSearch["after"]?:listOf())
        if (startAndEndDates == GetBoundingSearchDatesUseCase.Result.Invalid) return@withContext listOf()

        val nonCategorizedSearch = parsedSearch[null]?:listOf()
        val searchedPets =
            if (pickFrom is PickFrom.Pet) {
                val petsHashedSet = parsedSearch["pet"]?.toHashSet() ?: HashSet<String>()
                petsHashedSet.add(pickFrom.petName)
                petsHashedSet.toList()
            } else parsedSearch["pet"]?.toHashSet()?.toList()?:listOf()
        val searchedTags =
            if (pickFrom is PickFrom.Tag) {
                val tagsHashedSet = parsedSearch["#"]?.toHashSet() ?: HashSet<String>()
                tagsHashedSet.add(pickFrom.tagName)
                tagsHashedSet.toList()
            } else parsedSearch["#"]?.toHashSet()?.toList()?:listOf()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        // FTS4 matches
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("WITH matched_notes AS ( SELECT note_id FROM note_table_fts WHERE note_table_fts MATCH ? ) ")
            queryParams.add(nonCategorizedSearch.joinToString(separator=" "))
        }
        // Base query to join Note with associations
        queryBuilder.append("SELECT note_table.* FROM note_table ")
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("JOIN matched_notes ON note_table.note_id=matched_notes.note_id ")
        }
        pickFrom?.let {
            when (pickFrom) {
                is PickFrom.Photo -> {
                    queryBuilder.append("JOIN photo_note_table ON note_table.note_id=photo_note_table.note_id ")
                }
                is PickFrom.Event -> {
                    queryBuilder.append("JOIN event_note_table ON note_table.note_id=event_note_table.note_id ")
                }
                is PickFrom.Weight -> {
                    queryBuilder.append("JOIN weight_note_table ON note_table.note_id=weight_note_table.note_id ")
                }
                else -> {}
            }
        }
        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("JOIN pet_note_table ON note_table.note_id=pet_note_table.note_id ")
            queryBuilder.append("JOIN pet_table ON pet_table.pet_id=pet_note_table.pet_id ")
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("JOIN note_tag_table ON note_table.note_id=note_tag_table.note_id ")
            queryBuilder.append("JOIN tag_table ON tag_table.tag_id=note_tag_table.tag_id ")
        }

        // for pagination
        queryBuilder.append("WHERE (datetime(note_table.note_last_updated), note_table.note_id) < (datetime(?), ?) ")
        queryParams.add("${Converter.fromOffsetDateTime(lastNoteEditedDate)}")
        queryParams.add(lastNoteId)

        when (startAndEndDates) {
            is GetBoundingSearchDatesUseCase.Result.BoundingEndSearchDate -> {
                queryBuilder.append("AND datetime(note_table.note_last_updated) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingSearchDates -> {
                queryBuilder.append("AND datetime(note_table.note_last_updated) > datetime(?) ")
                queryBuilder.append("AND datetime(note_table.note_last_updated) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingStartSearchDate -> {
                queryBuilder.append("AND datetime(note_table.note_last_updated) > datetime(?) ")
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
        if (pickFrom is PickFrom.Photo) {
            queryBuilder.append("AND photo_note_table.photo_id = ? ")
            queryParams.add(pickFrom.photoId)
        }
        if (pickFrom is PickFrom.Weight) {
            queryBuilder.append("AND weight_note_table.weight_id = ? ")
            queryParams.add(pickFrom.weightId)
        }
        queryBuilder.append("GROUP BY note_table.note_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty()) {
            havingCountQuery.append("HAVING COUNT(DISTINCT pet_note_table.pet_id) = ? ")
            queryParams.add(searchedPets.size)
        }
        if (searchedTags.isNotEmpty()) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT note_tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size)
        }
        queryBuilder.append(havingCountQuery)

        queryBuilder.append("ORDER BY datetime(note_table.note_last_updated) DESC, note_table.note_id DESC LIMIT $notesAmt")

        val searchResults = noteDao.searchNotes(SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray()))

        lastNoteEditedDate = searchResults.lastOrNull()?.lastUpdated ?: OffsetDateTime.MIN
        lastNoteId = searchResults.lastOrNull()?.id ?: Long.MIN_VALUE
        _onLastPage = searchResults.size < notesAmt

        searchResults
    }

    override fun resetCurrentPoint() {
        lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }

    sealed class PickFrom {
        data class Pet(private val pet: LiveData<com.hfad.petlogger.pets.Pet>): PickFrom() {
            val petName: String get() = pet.value?.petName ?: ""
        }
        data class Event(val eventId: Long): PickFrom()
        data class Weight(val weightId: Long): PickFrom()
        data class Photo(val photoId: Long): PickFrom()
        data class Tag(private val tag: LiveData<com.hfad.petlogger.tags.Tag>): PickFrom() {
            val tagName: String get() = tag.value?.tagName ?: ""
        }
    }
}