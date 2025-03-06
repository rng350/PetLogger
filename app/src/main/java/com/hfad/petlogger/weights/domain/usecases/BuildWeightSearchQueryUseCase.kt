package com.hfad.petlogger.weights.domain.usecases

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.search.GetBoundingSearchDatesUseCase
import com.hfad.petlogger.common.search.ParseSearchQueryUseCase
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.tags.data.Tag
import java.time.OffsetDateTime

class BuildWeightSearchQueryUseCase(
    private val weightsAmt: Int,
    private val parseSearchQuery: ParseSearchQueryUseCase,
    private val getWeightFor: GetWeightFor,
    private val pickFrom: Pick?
) {
    suspend operator fun invoke(query: String, lastWeightDate: OffsetDateTime, lastWeightId: Long): SimpleSQLiteQuery? {
        val parsedSearch = parseSearchQuery(query)
        // get bounding dates
        val getBoundingSearchDates = GetBoundingSearchDatesUseCase()
        val startAndEndDates = getBoundingSearchDates(parsedSearch["before"]?:listOf(), parsedSearch["after"]?:listOf())
        if (startAndEndDates == GetBoundingSearchDatesUseCase.Result.Invalid) return null

        val nonCategorizedSearch = parsedSearch[null]?:listOf()

        // allow only one pet: prefix
        if ((parsedSearch["pet"]?.size ?: 0) > 1) {
            return null
        }
        val searchedPet = parsedSearch["pet"]?.toHashSet()?.toList()?.get(0)
        val searchedTags =
            if (pickFrom is Pick.FromTag) {
                val tagsHashedSet = parsedSearch["#"]?.toHashSet() ?: HashSet<String>()
                pickFrom.tag.value?.let {
                    tagsHashedSet.add(it.tagName)
                }
                tagsHashedSet.toList()
            } else parsedSearch["#"]?.toHashSet()?.toList()?:listOf()

        val queryBuilder = StringBuilder()
        val queryParams = mutableListOf<Any>()

        queryBuilder.append ("SELECT wt_1.weight_id AS weightId, wt_1.weight_Grams AS weightGramsAmt, wt_1.weight_datetime AS weightDateTime ")
        if (getWeightFor is GetWeightFor.GeneralDisplayList || getWeightFor is GetWeightFor.GeneralSelectionList) {
            queryBuilder.append(", pet_table.pet_name AS weightPetName ")
        }
        if (getWeightFor is GetWeightFor.GeneralDisplayList || getWeightFor is GetWeightFor.PetDisplayList) {
            queryBuilder.append(""", (
                            		SELECT wt_2.weight_grams 
                            		FROM weight_table wt_2 
                            		WHERE wt_2.weight_pet_id=wt_1.weight_pet_id 
                            		AND (datetime(wt_2.weight_datetime), wt_2.weight_id) < (datetime(wt_1.weight_datetime), wt_1.weight_id)
                            		ORDER BY datetime(wt_2.weight_datetime) DESC, wt_2.weight_id DESC LIMIT 1
                            	) AS prevWeightGramsAmt 
            """.trimIndent())
        }
        queryBuilder.append("FROM weight_table wt_1 ")
        if (getWeightFor is GetWeightFor.GeneralDisplayList || getWeightFor is GetWeightFor.GeneralSelectionList || searchedPet != null) {
            queryBuilder.append("JOIN pet_table ON wt_1.weight_pet_id=pet_table.pet_id ")
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("JOIN weight_tag_table ON wt_1.weight_id=weight_tag_table.weight_id ")
            queryBuilder.append("JOIN tag_table ON tag_table.tag_id=weight_tag_table.tag_id ")
        }
        queryBuilder.append("WHERE (datetime(wt_1.weight_datetime), wt_1.weight_id) < (datetime(?), ?) ")
        queryParams.add("${Converter.fromOffsetDateTime(lastWeightDate)}")
        queryParams.add(lastWeightId)

        when(startAndEndDates) {
            is GetBoundingSearchDatesUseCase.Result.BoundingEndSearchDate -> {
                queryBuilder.append("AND datetime(wt_1.weight_datetime) < ? ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingSearchDates -> {
                queryBuilder.append("AND datetime(wt_1.weight_datetime) > ? ")
                queryBuilder.append("AND datetime(wt_1.weight_datetime) < ? ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.endDate)}")
            }
            is GetBoundingSearchDatesUseCase.Result.BoundingStartSearchDate -> {
                queryBuilder.append("AND datetime(wt_1.weight_datetime) > ? ")
                queryParams.add("${Converter.fromOffsetDateTime(startAndEndDates.startDate)}")
            }
            else -> {}
        }
        searchedPet?.let { searchedPetName ->
            queryBuilder.append("AND pet_table.pet_name = ? ")
            queryParams.add(searchedPetName)
        }
        if (pickFrom is Pick.FromPet) {
            pickFrom.petId.let { petId ->
                queryBuilder.append("AND wt_1.weight_pet_id = ? ")
                queryParams.add(petId)
            }
        }
        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("AND tag_table.tag_name IN ${searchedTags.joinToString(prefix="(", separator=",", postfix=")"){"?"}} ")
            queryParams.addAll(searchedTags)
        }

        if (searchedTags.isNotEmpty()) {
            queryBuilder.append("GROUP BY wt_1.weight_id ")
            queryBuilder.append("HAVING COUNT(DISTINCT tag_table.tag_id) = ? ")
            queryParams.add(searchedTags.size)
        }

        queryBuilder.append("ORDER BY datetime(wt_1.weight_datetime) DESC, wt_1.weight_id DESC LIMIT ?")
        queryParams.add(weightsAmt)

        Log.d("WeightSearchQuery", "Query: $queryBuilder")
        Log.d("WeightSearchQuery", "Params: ${queryParams.map{it.toString()}}")

        return SimpleSQLiteQuery(queryBuilder.toString(), queryParams.toTypedArray())
    }

    sealed class Pick {
        data class FromTag(val tag: LiveData<Tag>): Pick()
        data class FromPet(val petId: Long): Pick()
    }

    sealed class GetWeightFor {
        data object GeneralSelectionList: GetWeightFor()
        data object GeneralDisplayList: GetWeightFor()
        data object PetDisplayList: GetWeightFor()
        data object PetSelectionList: GetWeightFor()
    }
}