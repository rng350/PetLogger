package com.hfad.petlogger.weights

import androidx.room.*
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.weights.WeightDetails
import com.hfad.petlogger.weights.WeightFullDetailsFetched
import com.hfad.petlogger.weights.WeightForListFetched
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface WeightDao {

    @Transaction
    suspend fun addWeight(weight: Weight): Weight {
        val weightRow = insert(weight)
        return getWeightFromRow(weightRow)
    }
    @Insert
    suspend fun insert(weight: Weight): Long

    @Query("SELECT * FROM weight_table WHERE rowid = :rowId")
    suspend fun getWeightFromRow(rowId: Long): Weight

    @Insert
    suspend fun insert(weights: List<Weight>)

    @Update
    suspend fun update(weight: Weight)

    @Delete
    suspend fun delete(weight: Weight)

    @Delete
    suspend fun delete(weights: List<Weight>)

    @Query("SELECT * FROM weight_table WHERE weight_id=:weightId")
    suspend fun get(weightId: Long): Weight

    @Query("SELECT * FROM weight_table ORDER BY weight_datetime DESC")
    suspend fun getAll(): MutableList<Weight>

    @Query("SELECT weight_table.weight_id AS weightId, " +
            "weight_table.weight_grams AS weightGramsAmt, " +
            "weight_table.weight_datetime AS weightDateTime, " +
            "pet_table.pet_name AS weightPetName, " +
            "photo_table.photo_uri AS weightPetPhotoUri " +
            "FROM weight_table " +
            "LEFT JOIN pet_table ON pet_table.pet_id=weight_table.weight_pet_id " +
            "LEFT JOIN pet_profile_photo_table ON pet_profile_photo_table.pet_id=pet_table.pet_id " +
            "LEFT JOIN photo_table ON photo_table.photo_id=pet_profile_photo_table.photo_id " +
            "ORDER BY weightDateTime DESC, weightId DESC")
    fun getWeightsWithPetNameAndPhoto(): Flow<List<WeightForListFetched>>

    @Query("""
            SELECT 
            wt_1.weight_id AS weightId,
            wt_1.weight_grams AS weightGramsAmt,
            wt_1.weight_datetime AS weightDateTime,
            pet_table.pet_name AS weightPetName, 
            (
                SELECT wt_2.weight_grams 
                FROM weight_table wt_2 
                WHERE wt_2.weight_pet_id=wt_1.weight_pet_id 
                AND (datetime(wt_2.weight_datetime), wt_2.weight_id) < (datetime(wt_1.weight_datetime), wt_1.weight_id)
                ORDER BY datetime(wt_2.weight_datetime) DESC, wt_2.weight_id DESC LIMIT 1
            ) AS prevWeightGramsAmt 
        FROM weight_table wt_1 
        LEFT JOIN pet_table ON wt_1.weight_pet_id=pet_table.pet_id 
        WHERE (datetime(wt_1.weight_datetime), wt_1.weight_id) < (datetime(:lastWeightDateTime), :lastWeightId) 
        ORDER BY datetime(wt_1.weight_datetime) DESC, wt_1.weight_id DESC LIMIT :amtLimit
    """)
    suspend fun getAllWeightsPaginated(lastWeightDateTime: OffsetDateTime, lastWeightId: Long, amtLimit: Int): List<WeightForListFetched>

    @Query("SELECT * FROM weight_table " +
            "WHERE weight_pet_id=:petId " +
            "AND weight_datetime < :weightDateTimeInString " +
            "ORDER BY weight_datetime DESC " +
            "LIMIT 1")
    fun getPreviousWeight(petId: Long, weightDateTimeInString: String): Flow<Weight?>

    @Transaction
    @Query("SELECT * FROM weight_table WHERE weight_id = :weightID")
    suspend fun getWeightDetails(weightID: Long): WeightDetails

    @Transaction
    @Query("SELECT * FROM weight_table")
    suspend fun getAllWeightDetails(): List<WeightDetails>

    @Transaction
    @Query("SELECT * FROM weight_table")
    fun getAllAsFlow(): Flow<WeightDetails>

    @Query("SELECT * FROM weight_table WHERE weight_id = :weightId LIMIT 1")
    fun getWeightAsFlow(weightId: Long): Flow<Weight>

    @Query("SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table " +
            "ON pet_profile_photo_table.pet_id=pet_table.pet_id " +
            "LEFT JOIN photo_table " +
            "ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "JOIN weight_table " +
            "ON pet_table.pet_id=weight_table.weight_pet_id " +
            "WHERE weight_table.weight_id=:weightId " +
            "LIMIT 1")
    fun getPetWithProfilePicOfWeight(weightId: Long): Flow<PetWithProfilePic>

    // TODO: there should be a faster query than this
    @Query("SELECT weight_table.* FROM weight_table " +
            "WHERE weight_pet_id IN (SELECT weight_pet_id FROM weight_table WHERE weight_id=:weightId LIMIT 1) " +
            "AND weight_datetime IN " +
            "(SELECT MAX(weight_datetime) FROM weight_table " +
            "WHERE weight_pet_id IN (SELECT weight_pet_id FROM weight_table WHERE weight_id =:weightId LIMIT 1) " +
            "AND weight_datetime < (SELECT weight_datetime FROM weight_table WHERE weight_id =:weightId LIMIT 1))")
    fun getPreviousWeight(weightId: Long): Flow<Weight>

    @Query("""
        WITH 
            cte_weight AS (SELECT * FROM weight_table WHERE weight_id=:weightId LIMIT 1),
            cte_pet_id AS (SELECT weight_pet_id FROM cte_weight LIMIT 1),
            cte_prev_weight AS (
                SELECT * 
                FROM weight_table 
                WHERE weight_pet_id IN cte_pet_id 
                AND (date(weight_datetime), weight_id) < ((SELECT date(weight_datetime) FROM cte_weight LIMIT 1), :weightId)
                ORDER BY date(weight_datetime) DESC, weight_id DESC LIMIT 1
            ),
            cte_weight_pet_and_pet_photo AS (
                SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri
                FROM pet_table 
                LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id 
                LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id 
                WHERE pet_table.pet_id IN cte_pet_id
                LIMIT 1
            )
        SELECT 
            cte_weight.weight_id AS curWeightId,
            cte_weight.weight_datetime AS curWeightDateTime,
            cte_weight.weight_grams AS curWeightGrams,
            cte_weight.weight_notes AS curWeightNotes,
            cte_prev_weight.weight_id AS prevWeightId,
            cte_prev_weight.weight_datetime AS prevWeightDateTime,
            cte_prev_weight.weight_grams AS prevWeightGrams,
            cte_weight_pet_and_pet_photo.petId AS petId,
            cte_weight_pet_and_pet_photo.petName AS petName,
            cte_weight_pet_and_pet_photo.petProfilePicUri AS petProfilePicUri
        FROM cte_weight 
        LEFT JOIN cte_prev_weight ON cte_weight.weight_pet_id=cte_prev_weight.weight_pet_id 
        LEFT JOIN cte_weight_pet_and_pet_photo ON cte_weight.weight_pet_id=cte_weight_pet_and_pet_photo.petId
        LIMIT 1
    """)
    fun getFullWeightDetails(weightId: Long): WeightFullDetailsFetched

    @Query("SELECT note_table.* " +
            "FROM note_table INNER JOIN weight_note_table " +
            "ON note_table.note_id=weight_note_table.note_id " +
            "WHERE weight_note_table.weight_id=:weightId " +
            "AND (datetime(note_table.note_last_updated), note_table.note_id) < (datetime(:lastNoteEditedDate), :lastNoteId) " +
            "ORDER BY note_table.note_last_updated DESC, note_table.note_id DESC LIMIT :amtLimit ")
    suspend fun getNotesOfWeightPaginated(weightId: Long, lastNoteEditedDate: OffsetDateTime, lastNoteId: Long, amtLimit: Int): List<Note>

    @Query("SELECT note_table.* " +
            "FROM note_table INNER JOIN weight_note_table " +
            "ON note_table.note_id=weight_note_table.note_id " +
            "WHERE weight_note_table.weight_id=:weightId " +
            "ORDER BY note_table.note_last_updated DESC, note_table.note_id DESC")
    suspend fun getNotesOfWeight(weightId: Long): List<Note>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN weight_tag_table 
        ON tag_table.tag_id=weight_tag_table.tag_id 
        WHERE weight_tag_table.weight_id=:weightId
    """)
    suspend fun getTagsOfWeight(weightId: Long): List<Tag>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN weight_tag_table 
        ON tag_table.tag_id=weight_tag_table.tag_id 
        WHERE weight_tag_table.weight_id=:weightId 
        ORDER BY tag_table.tag_name ASC
    """)
    suspend fun getTagsOfWeightAlphabeticalOrder(weightId: Long): List<Tag>
}