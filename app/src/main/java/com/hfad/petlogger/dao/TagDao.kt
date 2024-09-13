package com.hfad.petlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.hfad.petlogger.entities.EventTag
import com.hfad.petlogger.entities.NoteTag
import com.hfad.petlogger.entities.PetTag
import com.hfad.petlogger.entities.PhotoTag
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.entities.WeightTag

@Dao
interface TagDao {
    @Insert
    suspend fun insert(tag: Tag): Long

    @Query("SELECT * FROM tag_table WHERE rowid = :rowId")
    suspend fun getTagFromRowId(rowId: Long): Tag

    @Query("SELECT * FROM tag_table")
    suspend fun getAllTags(): List<Tag>

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

    @RawQuery
    suspend fun getIdsByQuery(query: SupportSQLiteQuery): List<Long>

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
}