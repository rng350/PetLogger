package com.hfad.petlogger.pets.usecases

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.tags.Tag

class BuildPetSearchQueryUseCase(
    private val get: Get = Get.PetWithProfilePic,
    private val petsAmt: Int? = null,
    private val pickFrom: Pick? = null
) {
    private val parseSearchQuery = ParseSearchQueryUseCase(listOf("name", "species", "breed", "sex"))
    operator fun invoke(
        query: String,
        lastPetId: Long = Long.MIN_VALUE
    ): SimpleSQLiteQuery? {
        val parsedSearch = parseSearchQuery(query)
        val nonCategorizedSearch = parsedSearch[null]?:listOf()
        val searchedTags =
            if (pickFrom is Pick.FromTag && pickFrom.tagName.isNotEmpty()) {
                val tagsHashedSet = parsedSearch["#"]?.toHashSet() ?: HashSet<String>()
                tagsHashedSet.add(pickFrom.tagName)
                tagsHashedSet.toList()
            } else parsedSearch["#"]?.toHashSet()?.toList()?:listOf()
        val searchedPetName = parsedSearch["name"]?.firstOrNull()
        val searchedSpecies = parsedSearch["species"]?.firstOrNull()
        val searchedBreed = parsedSearch["breed"]?.firstOrNull()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        // FTS4 matches
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("WITH matched_pets AS ( SELECT pet_id FROM pet_fts_table WHERE pet_fts_table MATCH ? )")
            queryParams.add(nonCategorizedSearch.joinToString(separator=" "))
        }

        when(get) {
            Get.OnlyIds -> {
                queryBuilder.append("SELECT pet_table.pet_id ")
            }
            Get.PetWithProfilePic -> {
                queryBuilder.append("SELECT pet_table.pet_name AS petName, pet_table.pet_id AS petId, photo_table.photo_uri AS petProfilePicUri ")
            }
        }
        queryBuilder.append("FROM pet_table ")
        if (nonCategorizedSearch.isNotEmpty()) {
            queryBuilder.append("JOIN matched_pets ON pet_table.pet_id = matched_pets.pet_id ")
        }
        when (pickFrom) {
            is Pick.FromEvent -> {
                queryBuilder.append("JOIN event_pet_table ON pet_table.pet_id = event_pet_table.pet_id ")
            }
            is Pick.FromNote -> {
                queryBuilder.append("JOIN pet_note_table ON pet_table.pet_id = pet_note_table.pet_id ")
            }
            is Pick.FromPhoto -> {
                queryBuilder.append("JOIN pet_photo_table ON pet_table.pet_id = pet_photo_table.pet_id ")
            }
            else -> {}
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("JOIN pet_tag_table ON pet_table.pet_id=pet_tag_table.pet_id ")
            queryBuilder.append("JOIN tag_table ON tag_table.tag_id=pet_tag_table.tag_id ")
        }

        queryBuilder.append("LEFT JOIN pet_profile_photo_table ON pet_table.pet_id = pet_profile_photo_table.pet_id ")
        queryBuilder.append("LEFT JOIN photo_table ON pet_profile_photo_table.photo_id = photo_table.photo_id ")

        // for pagination
        queryBuilder.append("WHERE pet_table.pet_id > ? ")
        queryParams.add(lastPetId)

        searchedPetName?.let {
            queryBuilder.append("AND pet_table.pet_name = ? ")
            queryParams.add(searchedPetName)
        }
        searchedSpecies?.let {
            queryBuilder.append("AND pet_table.pet_species = ? ")
            queryParams.add(searchedSpecies)
        }
        searchedBreed?.let {
            queryBuilder.append("AND pet_table.pet_breed = ? ")
            queryParams.add(searchedBreed)
        }

        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("AND tag_table.tag_name IN ${searchedTags.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedTags)
        }
        when (pickFrom) {
            is Pick.FromEvent -> {
                queryBuilder.append("AND event_pet_table.event_id = ? ")
                queryParams.add(pickFrom.eventId)
            }
            is Pick.FromNote -> {
                queryBuilder.append("AND pet_note_table.note_id = ? ")
                queryParams.add(pickFrom.noteId)
            }
            is Pick.FromPhoto -> {
                queryBuilder.append("AND pet_photo_table.photo_id = ? ")
                queryParams.add(pickFrom.photoId)
            }
            else -> {}
        }

        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("GROUP BY pet_table.pet_id ")
            queryBuilder.append("HAVING COUNT(DISTINCT tag_table.tag_name) = ? ")
            queryParams.add(searchedTags.size)
        }

        queryBuilder.append("ORDER BY pet_table.pet_id ASC ")
        petsAmt?.let {
            queryBuilder.append("LIMIT ? ")
            queryParams.add(petsAmt)
        }

        Log.d("PetSearchQuery", "Query: $queryBuilder")
        Log.d("PetSearchQuery", "Params: ${queryParams.map{it.toString()}}")

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }

    sealed class Pick {
        data class FromEvent(val eventId: Long): Pick()
        data class FromNote(val noteId: Long): Pick()
        data class FromPhoto(val photoId: Long): Pick()
        data class FromTag(val tag: LiveData<Tag>): Pick() {
            val tagName: String get() = tag.value?.tagName ?: ""
        }
    }

    sealed class Get {
        object OnlyIds: Get()
        object PetWithProfilePic: Get()
    }
}