package com.hfad.petlogger.tags

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.common.associationentities.EventTag
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.common.associationentities.NoteTag
import com.hfad.petlogger.common.associationentities.PetTag
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.associationentities.PhotoTag
import com.hfad.petlogger.weights.WeightForListFetched
import com.hfad.petlogger.common.associationentities.WeightTag
import com.hfad.petlogger.common.util.Constants.Companion.noteIdField
import com.hfad.petlogger.common.util.Constants.Companion.noteTagTableHeader
import com.hfad.petlogger.common.util.Constants.Companion.tagIdField
import com.hfad.petlogger.common.util.Constants.Companion.tagNameField
import com.hfad.petlogger.common.util.Constants.Companion.tagTableHeader
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tag_table WHERE tag_id=:tagId")
    suspend fun getTag(tagId: Long): Tag

    @Insert
    suspend fun insert(tag: Tag): Long

    @Query("SELECT * FROM tag_table WHERE rowid = :rowId")
    suspend fun getTagFromRowId(rowId: Long): Tag

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Insert
    suspend fun attachPet(petTag: PetTag)

    @Delete
    suspend fun detachPet(petTag: PetTag)

    @Insert
    suspend fun attachEvent(eventTag: EventTag)

    @Delete
    suspend fun detachEvent(eventTag: EventTag)

    @Insert
    suspend fun attachWeight(weightTag: WeightTag)

    @Delete
    suspend fun detachWeight(weightTag: WeightTag)

    @Insert
    suspend fun attachPhoto(photoTag: PhotoTag)

    @Delete
    suspend fun detachPhoto(photoTag: PhotoTag)

    @Insert
    suspend fun attachNote(noteTag: NoteTag)

    @Delete
    suspend fun detachNote(noteTag: NoteTag)

    @Query("SELECT * FROM tag_table WHERE tag_name=:tagName")
    suspend fun getTagByName(tagName: String): Tag?

    suspend fun getPetIdsWithTags(tagIds: List<Long>): List<Long> {
        return getEntityIdsWithTags(tagIds, "pet_tag_table", "pet_id")
    }

    suspend fun getEventIdsWithTags(tagIds: List<Long>): List<Long> {
        return getEntityIdsWithTags(tagIds, "event_tag_table", "event_id")
    }

    suspend fun getWeightIdsWithTags(tagIds: List<Long>): List<Long> {
        return getEntityIdsWithTags(tagIds, "weight_tag_table", "weight_id")
    }

    suspend fun getNoteIdsWithTags(tagIds: List<Long>): List<Long> {
        return getEntityIdsWithTags(tagIds, "note_tag_table", "note_id")
    }

    suspend fun getPhotoIdsWithTags(tagIds: List<Long>): List<Long> {
        return getEntityIdsWithTags(tagIds, "photo_tag_table", "photo_id")
    }

    suspend fun getEntityIdsWithTags(tagIds: List<Long>, tableName: String, entityIdFieldName: String): List<Long> {
        val tagIdsList = tagIds.joinToString(prefix = "(", postfix = ")", separator = ", ") { "?" }
        val args = tagIds.map { "$it" }.toTypedArray()
        val query = SimpleSQLiteQuery("SELECT $entityIdFieldName FROM $tableName WHERE tag_id IN $tagIdsList GROUP BY $entityIdFieldName HAVING COUNT(*)=${tagIds.size}", args)
        return getIdsByQuery(query)
    }

    @RawQuery
    suspend fun getIdsByQuery(query: SupportSQLiteQuery): List<Long>

    @Query("""
        SELECT *, 
            CASE
                WHEN tag_name = :query THEN 0
                WHEN tag_name LIKE :query || '%' THEN 1 
                WHEN tag_name LIKE '%' || :query THEN 2 
                WHEN tag_name LIKE '%' || :query || '%' THEN 3 
                ELSE 4 
            END AS relevance, 
            LENGTH(tag_name) AS length 
        FROM tag_table 
        WHERE tag_name LIKE '%' || :query || '%' 
        ORDER BY relevance ASC, length ASC
    """)
    suspend fun searchTagsByQuery(query: String): List<Tag>

    @Query("""
        SELECT 
            IFNULL(COUNT(nt.tag_id), 0) + 
            IFNULL(COUNT(et.tag_id), 0) + 
            IFNULL(COUNT(pt.tag_id), 0) + 
            IFNULL(COUNT(mt.tag_id), 0) + 
            IFNULL(COUNT(wt.tag_id), 0)
        FROM tag_table t 
        LEFT JOIN note_tag_table nt ON t.tag_id=nt.tag_id 
        LEFT JOIN event_tag_table et ON t.tag_id=et.tag_id 
        LEFT JOIN pet_tag_table pt ON t.tag_id=pt.tag_id 
        LEFT JOIN photo_tag_table mt ON t.tag_id=mt.tag_id 
        LEFT JOIN weight_tag_table wt ON t.tag_id=wt.tag_id 
        WHERE t.tag_id = :tagId
    """)
    suspend fun countTagInstances(tagId: Long): Int

    @Query("""
        SELECT t.tag_id, t.tag_name, 
            IFNULL(COUNT(nt.tag_id), 0) + 
            IFNULL(COUNT(et.tag_id), 0) + 
            IFNULL(COUNT(pt.tag_id), 0) + 
            IFNULL(COUNT(mt.tag_id), 0) + 
            IFNULL(COUNT(wt.tag_id), 0) AS total_count 
        FROM tag_table t 
        LEFT JOIN note_tag_table nt ON t.tag_id=nt.tag_id 
        LEFT JOIN event_tag_table et ON t.tag_id=et.tag_id 
        LEFT JOIN pet_tag_table pt ON t.tag_id=pt.tag_id 
        LEFT JOIN photo_tag_table mt ON t.tag_id=mt.tag_id 
        LEFT JOIN weight_tag_table wt ON t.tag_id=wt.tag_id 
        GROUP BY t.tag_id 
        ORDER BY total_count DESC
    """)
    suspend fun getAllTagsOrderedByFrequency(): List<Tag>

    @Query("SELECT * FROM tag_table ORDER BY tag_name ASC LIMIT :amtLimit")
    suspend fun getAllTagsAlphabeticalOrder(amtLimit: Int): List<Tag>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN note_tag_table 
        ON note_tag_table.tag_id=tag_table.tag_id 
        WHERE note_tag_table.note_id=:noteId
    """)
    suspend fun getAllTagsOfNote(noteId: Long): List<Tag>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN note_tag_table 
        ON note_tag_table.tag_id=tag_table.tag_id 
        WHERE note_tag_table.note_id=:noteId 
        ORDER BY tag_table.tag_name ASC
    """)
    suspend fun getAllTagsOfNoteAlphabeticalOrder(noteId: Long): List<Tag>

    @Query("SELECT tag_table.tag_id AS tagId, tag_table.tag_name AS tagName, 0 as isChecked FROM tag_table ORDER BY tag_name ASC")
    suspend fun getAllCheckedTags(): List<CheckableTagFetched>

    suspend fun getCheckedTagSelectionOptionsOfNote(noteId: Long): List<CheckableTagFetched> {
        return getCheckedTagSelectionOptionsOfEntity(
            associationTableName = noteTagTableHeader,
            associatedEntityIdFieldName = noteIdField,
            associatedEntityId = noteId
        )
    }

    private suspend fun getCheckedTagSelectionOptionsOfEntity(associationTableName: String,
                                       associatedEntityIdFieldName: String,
                                       associatedEntityId: Long): List<CheckableTagFetched> {
        val query = SimpleSQLiteQuery(
            """
                SELECT $tagTableHeader.$tagIdField AS tagId, $tagTableHeader.$tagNameField AS tagName,  
                    CASE 
                        WHEN $associationTableName.$associatedEntityIdFieldName = NULL THEN 0 
                        ELSE 1 
                    END AS isChecked 
                FROM $tagTableHeader 
                LEFT JOIN $associationTableName 
                ON $tagTableHeader.$tagIdField = $associationTableName.$tagIdField 
                WHERE $associationTableName.$associatedEntityIdFieldName = ${associatedEntityId.toString()}
            """.trimIndent()
        )
        return getCheckedTagSelectionOptionsOfEntityByQuery(query)
    }
    @RawQuery
    suspend fun getCheckedTagSelectionOptionsOfEntityByQuery(query: SupportSQLiteQuery): List<CheckableTagFetched>

    @Query("""
        SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri 
        FROM pet_table 
        LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id 
        LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id 
        LEFT JOIN pet_tag_table ON pet_table.pet_id=pet_tag_table.pet_id 
        WHERE pet_tag_table.tag_id=:tagId 
        ORDER BY pet_table.pet_id ASC
    """)
    fun getPetsOfTag(tagId: Long): Flow<List<PetWithProfilePic>>

    @Query("""
        SELECT event_table.* 
        FROM event_table 
        LEFT JOIN event_tag_table ON event_table.event_id=event_tag_table.event_id 
        WHERE event_tag_table.tag_id=:tagId 
        ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC
    """)
    fun getEventsOfTag(tagId: Long): Flow<List<Event>>

    @Query("""
        SELECT note_table.* 
        FROM note_table 
        LEFT JOIN note_tag_table ON note_table.note_id=note_tag_table.note_id 
        WHERE note_tag_table.tag_id=:tagId 
        ORDER BY datetime(note_table.note_last_updated) DESC, note_table.note_id DESC
    """)
    fun getNotesOfTag(tagId: Long): Flow<List<Note>>

    @Query("""
        SELECT photo_table.* 
        FROM photo_table 
        LEFT JOIN photo_tag_table ON photo_table.photo_id=photo_tag_table.photo_id 
        WHERE photo_tag_table.tag_id=:tagId 
        ORDER BY datetime(photo_table.photo_date) DESC, photo_table.photo_id DESC
    """)
    fun getPhotosOfTag(tagId: Long): Flow<List<Photo>>

    @Query("""
        SELECT weight_table.weight_id AS weightId, 
            weight_table.weight_grams AS weightGramsAmt, 
            weight_table.weight_datetime AS weightDateTime, 
            pet_table.pet_name AS weightPetName 
        FROM weight_table 
        LEFT JOIN weight_tag_table ON weight_table.weight_id=weight_tag_table.weight_id 
        LEFT JOIN pet_table ON weight_table.weight_pet_id=pet_table.pet_id
        WHERE weight_tag_table.tag_id=:tagId 
        ORDER BY datetime(weight_table.weight_datetime) DESC, weight_table.weight_id DESC
    """)
    fun getWeightsOfTag(tagId: Long): Flow<List<WeightForListFetched>>
}