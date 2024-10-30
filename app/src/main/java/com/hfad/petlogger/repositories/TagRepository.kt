package com.hfad.petlogger.repositories

import android.util.Log
import androidx.room.withTransaction
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventTag
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.NoteTag
import com.hfad.petlogger.entities.PetTag
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoTag
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.entities.WeightTag
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.Constants.Companion.newTagPlaceholderId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class TagRepository(private val database: PetLoggerDatabase) {
    private val tagDao = database.tagDao

    suspend fun getTagByName(tagName: String): Tag? = withContext(Dispatchers.IO) {
        tagDao.getTagByName(tagName)
    }

    suspend fun getAllTags(): List<Tag> = withContext(Dispatchers.IO) {
        tagDao.getAllTagsOrderedByFrequency()
    }

    suspend fun getTagsOfNote(noteId: Long): List<Tag> = withContext(Dispatchers.IO) {
        tagDao.getAllTagsOfNote(noteId)
    }

    suspend fun getTagsOfNoteAlphabeticalOrder(noteId: Long): List<Tag> = withContext(Dispatchers.IO) {
        tagDao.getAllTagsOfNoteAlphabeticalOrder(noteId)
    }

    suspend fun updateTag(
        associatedPets: List<PetWithProfilePic> = listOf<PetWithProfilePic>(),
        associatedEvents: List<Event> = listOf<Event>(),
        associatedNotes: List<Note> = listOf<Note>(),
        associatedWeights: List<WeightWithPetName> = listOf<WeightWithPetName>(),
        associatedPhotos: List<Photo> = listOf<Photo>()
    ) = withContext(Dispatchers.IO) {
        // step 1: count tags
        // step 2: if entities to add == 0 && entities to remove == tag count, just delete the tag
        // otherwise, proceed
    }
    suspend fun attachPetToTag(petId: Long, tagName: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val tag = tagDao.getTagByName(tagName)
            if (tag != null) {
                tagDao.attachPet(PetTag(petId = petId, tagId = tag.tagId))
            } else {
                    val rowId = tagDao.insert(Tag(tagName = tagName))
                    val insertedTag = tagDao.getTagFromRowId(rowId)
                    tagDao.attachPet(PetTag(petId = petId, tagId = insertedTag.tagId))
            }
        }
    }

    suspend fun attachPetToNewTag(petId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val insertedTag = insertNewTag(tag)
            tagDao.attachPet(PetTag(petId = petId, tagId = insertedTag.tagId))
        }
    }

    suspend fun attachPetToExistingTag(petId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        tagDao.attachPet(PetTag(petId = petId, tagId = tag.tagId))
    }

    suspend fun attachEventToNewTag(eventId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val insertedTag = insertNewTag(tag)
            tagDao.attachEvent(EventTag(eventId = eventId, tagId = insertedTag.tagId))
        }
    }

    suspend fun attachEventToExistingTag(eventId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        tagDao.attachEvent(EventTag(eventId = eventId, tagId = tag.tagId))
    }

    suspend fun attachNoteToExistingTag(noteId: Long, tag: Tag) {
        tagDao.attachNote(NoteTag(noteId = noteId, tagId = tag.tagId))
    }

    suspend fun attachNoteToNewTag(noteId: Long, tag: Tag) {
        database.withTransaction {
            val insertedTag = insertNewTag(tag)
            tagDao.attachNote(NoteTag(noteId = noteId, tagId = insertedTag.tagId))
        }
    }

    private suspend fun insertNewTag(tag: Tag): Tag {
        val rowId = tagDao.insert(Tag(tagName = tag.tagName))
        return tagDao.getTagFromRowId(rowId)
    }

    suspend fun attachNoteToTag(noteId: Long, tagName: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val tag = tagDao.getTagByName(tagName)
            if (tag != null) {
                tagDao.attachNote(NoteTag(noteId = noteId, tagId = tag.tagId))
            } else {
                val rowId = tagDao.insert(Tag(tagName = tagName))
                val insertedTag = tagDao.getTagFromRowId(rowId)
                tagDao.attachNote(NoteTag(noteId = noteId, tagId = insertedTag.tagId))
            }
        }
    }

    suspend fun detachNoteFromTag(noteId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        if (!shouldDeleteTag(tag))
            tagDao.detachNote(NoteTag(noteId = noteId, tagId = tag.tagId))
    }

    suspend fun detachPetFromTag(petId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        if (!shouldDeleteTag(tag))
            tagDao.detachPet(PetTag(petId = petId, tagId = tag.tagId))
    }

    suspend fun detachEventFromTag(eventId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        if (!shouldDeleteTag(tag))
            tagDao.detachEvent(EventTag(eventId = eventId, tagId = tag.tagId))
    }

    suspend fun detachWeightFromTag(weightId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        if (!shouldDeleteTag(tag))
            tagDao.detachWeight(WeightTag(weightId = weightId, tagId = tag.tagId))
    }

    suspend fun detachPhotoFromTag(photoId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        if (!shouldDeleteTag(tag))
            tagDao.detachPhoto(PhotoTag(photoId = photoId, tagId = tag.tagId))
    }

    private suspend fun shouldDeleteTag(tag: Tag): Boolean {
        val tagInstancesAmt = tagDao.countTagInstances(tag.tagId)
        return if (tagInstancesAmt < 2) {
            tagDao.delete(tag)
            true
        } else false
    }

    suspend fun searchTagsByQuery(query: String): List<Tag> = withContext(Dispatchers.IO) {
        var searchResults = tagDao.searchTagsByQuery(query)
        if (searchResults.isNotEmpty()) {
            if (searchResults[0].tagName != query) {
                searchResults = listOf(Tag(tagName = query, tagId = newTagPlaceholderId)) + searchResults
            }
        } else {
            searchResults = listOf(Tag(tagName = query, tagId = newTagPlaceholderId))
        }
        searchResults
    }

    suspend fun getCheckedTagSelectionOptionsOfNote(noteId: Long?): List<CheckableItem<Tag>> = withContext(Dispatchers.IO) {
        if (noteId != null) {
            tagDao.getCheckedTagSelectionOptionsOfNote(noteId).map { it.toCheckableItem() }
        } else {
            tagDao.getAllCheckedTags().map { it.toCheckableItem() }
        }
    }

    // TODO: attachEventToTag
    // TODO: attachWeightToTag
    // TODO: attachPhotoToTag
}