package com.hfad.petlogger.repositories

import androidx.room.withTransaction
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.entities.PetTag
import com.hfad.petlogger.entities.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TagRepository(private val database: PetLoggerDatabase) {
    private val tagDao = database.tagDao
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

    // TODO: attachEventToTag
    // TODO: attachNoteToTag
    // TODO: attachWeightToTag
    // TODO: attachPhotoToTag

    // TODO: detachPetFromTag
    // TODO: detachEventFromTag
    // TODO: detachNoteFromTag
    // TODO: detachWeightFromTag
    // TODO: detachPhotoFromTag
}