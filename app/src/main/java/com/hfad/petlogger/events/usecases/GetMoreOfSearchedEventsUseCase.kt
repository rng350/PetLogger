package com.hfad.petlogger.events.usecases

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.events.EventForList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMoreOfSearchedEventsUseCase(
    private val eventDao: EventDao,
    private val eventAmt: Int,
    private val pickFrom: Pick? = null
): GetSearchedItemsUseCase<EventForList> {
    override var currentQuery: String = ""
    private var lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastEventId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("pet", "before", "after"))
    override suspend fun invoke(): List<EventForList> = withContext(Dispatchers.IO) {
        val parsedSearch = parseSearchQuery(currentQuery)
        // get bounding dates
        val getBoundingSearchDates = GetBoundingSearchDatesUseCase()
        val startAndEndDates = getBoundingSearchDates(parsedSearch["before"]?:listOf(), parsedSearch["after"]?:listOf())
        if (startAndEndDates == GetBoundingSearchDatesUseCase.Result.Invalid) return@withContext listOf()

        val nonCategorizedSearch = parsedSearch[null]?:listOf()
        val searchedPets =
            if (pickFrom is Pick.FromPet) {
                val petsHashedSet = parsedSearch["pet"]?.toHashSet() ?: HashSet<String>()
                petsHashedSet.add(pickFrom.petName)
                petsHashedSet.toList()
            } else parsedSearch["pet"]?.toHashSet()?.toList()?:listOf()
        val searchedTags =
            if (pickFrom is Pick.FromTag) {
                val tagsHashedSet = parsedSearch["#"]?.toHashSet() ?: HashSet<String>()
                tagsHashedSet.add(pickFrom.tagName)
                tagsHashedSet.toList()
            } else parsedSearch["#"]?.toHashSet()?.toList()?:listOf()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        // FTS4 matches
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("WITH matched_events AS ( SELECT event_id FROM event_table_fts WHERE event_table_fts MATCH ? ) ")
            queryParams.add(nonCategorizedSearch.joinToString(separator=" "))
        }
        // Base query to join Event with associations
        queryBuilder.append("SELECT event_table.* FROM event_table ")
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("JOIN matched_events ON event_table.event_id=matched_events.event_id ")
        }
        pickFrom?.let {
            when (pickFrom) {
                is Pick.FromNote -> {
                    queryBuilder.append("JOIN event_note_table ON event_table.event_id=event_note_table.event_id ")
                }
                is Pick.FromPhoto -> {
                    queryBuilder.append("JOIN photo_event_table ON event_table.event_id=photo_event_table.event_id ")
                }
                else -> {}
            }
        }

        if (searchedPets.isNotEmpty()) {
            queryBuilder.append("JOIN event_pet_table ON event_table.event_id = event_pet_table.event_id ")
            queryBuilder.append("JOIN pet_table ON pet_table.pet_id = event_pet_table.pet_id ")
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("JOIN event_tag_table ON event_table.event_id = event_tag_table.event_id ")
            queryBuilder.append("JOIN tag_table ON tag_table.tag_id = event_tag_table.tag_id ")
        }

        queryBuilder.append("WHERE (datetime(event_table.event_date), event_table.event_id) < (datetime(?), ?) ")
        queryParams.add("${Converter.fromOffsetDateTime(lastEventDate)}")
        queryParams.add(lastEventId)

        when (startAndEndDates) {
            is GetBoundingSearchDatesUseCase.Result.BoundingEndSearchDate -> {
                queryBuilder.append("AND datetime(event_table.event_date) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingSearchDates -> {
                queryBuilder.append("AND datetime(event_table.event_date) > datetime(?) ")
                queryBuilder.append("AND datetime(event_table.event_date) < datetime(?) ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingStartSearchDate -> {
                queryBuilder.append("AND datetime(event_table.event_date) > datetime(?) ")
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
        if (pickFrom is Pick.FromPhoto) {
            queryBuilder.append("AND photo_event_table.photo_id = ? ")
            queryParams.add(pickFrom.photoId)
        }
        if (pickFrom is Pick.FromNote) {
            queryBuilder.append("AND event_note_table.note_id = ? ")
            queryParams.add(pickFrom.noteId)
        }
        queryBuilder.append("GROUP BY event_table.event_id ")

        val havingCountQuery = StringBuilder()
        if (searchedPets.isNotEmpty()) {
            havingCountQuery.append("HAVING COUNT(DISTINCT event_pet_table.pet_id) = ? ")
            queryParams.add(searchedPets.size)
        }
        if (searchedTags.isNotEmpty()) {
            havingCountQuery.append("${if (havingCountQuery.isNotEmpty()) "AND" else "HAVING"} COUNT(DISTINCT event_tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size)
        }
        queryBuilder.append(havingCountQuery)

        queryBuilder.append("ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC LIMIT $eventAmt")

        val searchResults = eventDao.searchEvents(SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray()))

        // update for further pagination
        lastEventId = searchResults.lastOrNull()?.eventId ?: Long.MAX_VALUE
        lastEventDate = searchResults.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = searchResults.size < eventAmt

        searchResults.map{it.toEventForList()}
    }

    override fun resetCurrentPoint() {
        lastEventDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastEventId = Long.MAX_VALUE
        _onLastPage = false
    }

    sealed class Pick {

        data class FromPet(private val pet: LiveData<com.hfad.petlogger.pets.Pet>): Pick() {
            val petName: String get() = pet.value?.petName ?: ""
        }
        data class FromNote(val noteId: Long): Pick()
        data class FromPhoto(val photoId: Long): Pick()
        data class FromTag(private val tag: LiveData<com.hfad.petlogger.tags.Tag>): Pick() {
            val tagName: String get() = tag.value?.tagName ?: ""
        }
    }
}