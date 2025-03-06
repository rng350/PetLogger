package com.hfad.petlogger.events.domain

import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.common.associationentities.EventNote
import com.hfad.petlogger.common.associationentities.EventPet
import com.hfad.petlogger.common.associationentities.PhotoEvent
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.events.data.EventDao
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.domain.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class EventRepository(private val database: PetLoggerDatabase,
                      private val mediaRepository: MediaRepository
) {
    private val eventDao: EventDao = database.eventDao
    suspend fun get(eventId: Long): Event = withContext(Dispatchers.IO) {
        eventDao.get(eventId)
    }

    suspend fun getAll(): List<Event> = withContext(Dispatchers.IO) {
        eventDao.getAll()
    }

    suspend fun getPetsOfEvent(eventId: Long): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        eventDao.getPetsOfEventWithProfilePhotos(eventId)
    }

    suspend fun insert(event: Event,
                       pets: List<Long> = listOf<Long>(),
                       photos: List<Photo> = listOf<Photo>(),
                       notes: List<Note> = listOf<Note>(),
                       tags: List<Tag> = listOf<Tag>(),
    ) = withContext(Dispatchers.IO) {
        val eventId = async {
            eventDao.insert(event)
        }.await()
        val addEventPets = pets.map {petID ->
            async {
                addEventPet(EventPet(eventId, petID))
            }
        }
        val addEventPhotos = photos.map {photo ->
            async {
                mediaRepository.addNewPhotoForEvent(photo, eventId)
            }
        }
        val attachNotes = async {
            eventDao.attachNotes(notes.map{ note -> EventNote(eventId = eventId, noteId=note.id) })
        }
        val tagRepository = TagRepository(database)
        val attachTags = tags.map { tag ->
            async {
                attachTagToEvent(tagRepository, eventId, tag)
            }
        }
        attachNotes.await()
        addEventPets.awaitAll()
        addEventPhotos.awaitAll()
        attachTags.awaitAll()

        eventId
    }

    suspend fun update(event: Event,
                       petsToAdd: List<Long> = listOf<Long>(),
                       petsToRemove: List<Long> = listOf<Long>(),
                       photosToAdd: List<Photo> = listOf<Photo>(),
                       photosToRemove: List<Photo> = listOf<Photo>(),
                       notesToAdd: List<Note> = listOf<Note>(),
                       notesToRemove: List<Note> = listOf<Note>(),
                       tagsToAdd: List<Tag> = listOf<Tag>(),
                       tagsToRemove: List<Tag> = listOf<Tag>())
    = withContext(Dispatchers.IO) {
        val eventUpdated = async {
            eventDao.update(event)
        }
        val petDao = database.petDao
        val petsAdded = async {
            petDao.insert(petsToAdd.map{petID -> EventPet(eventId=event.eventId, petId=petID) })
        }
        val petsDeleted = async {
            petDao.delete(petsToRemove.map{petID -> EventPet(eventId=event.eventId, petId=petID) })
        }
        val photoDao = database.photoDao
        val photosAdded = photosToAdd.map { newPhoto ->
            async {
                mediaRepository.addNewPhotoForEvent(newPhoto, event.eventId)
            }
        }
        val photosDeleted =  async {
            photoDao.delete(photosToRemove.map{ photoToRemove -> PhotoEvent(photoID = photoToRemove.id, eventID = event.eventId) })
        }
        val notesAttached = async {
            eventDao.attachNotes(notesToAdd.map{note -> EventNote(eventId=event.eventId, noteId=note.id) })
        }
        val notesDetached = async {
            eventDao.detachNotes(notesToRemove.map{note -> EventNote(eventId=event.eventId, noteId=note.id) })
        }

        val tagRepository = TagRepository(database)
        val tagsAdded = tagsToAdd.map { tag ->
            async {
                attachTagToEvent(tagRepository, event.eventId, tag)
            }
        }
        val tagsRemoved = tagsToRemove.map { tag ->
            async {
                tagRepository.detachEventFromTag(event.eventId, tag)
            }
        }

        notesAttached.await()
        notesDetached.await()
        eventUpdated.await()
        petsAdded.await()
        petsDeleted.await()
        photosDeleted.await()
        photosAdded.awaitAll()
        tagsAdded.awaitAll()
        tagsRemoved.awaitAll()
    }

    private suspend fun attachTagToEvent(tagRepository: TagRepository, eventId: Long, tag: Tag) {
        if (tag.tagId == Constants.newTagPlaceholderId) {
            tagRepository.attachEventToNewTag(eventId, tag)
        }
        else tagRepository.attachEventToExistingTag(eventId, tag)
    }

    suspend fun delete(event: Event) {
        withContext(Dispatchers.IO) {
            eventDao.delete(event)
        }
    }

    suspend fun addEventPet(eventPet: EventPet) = withContext(Dispatchers.IO) {
        val eventPetDao = database.eventPetDao
        eventPetDao.insert(eventPet)
    }

    suspend fun getPhotosOfEvent(eventId: Long): List<Photo> = withContext(Dispatchers.IO) {
        eventDao.fetchPhotosOfEvent(eventId)
    }

    suspend fun getNotesOfEvent(eventId: Long): List<Note> = withContext(Dispatchers.IO) {
        eventDao.getNotesOfEvent(eventId)
    }

    suspend fun getNotesOfEventPaginated(eventId: Long, lastNoteEditedDate: OffsetDateTime, lastNoteId: Long, amtLimit: Int): List<Note> = withContext(Dispatchers.IO) {
        eventDao.getNotesOfEventPaginated(eventId, lastNoteEditedDate, lastNoteId, amtLimit)
    }

    suspend fun getPhotosOfEventPaginated(eventId: Long, lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo> = withContext(Dispatchers.IO) {
        eventDao.getPhotosOfEventPaginated(eventId, lastPhotoDate, lastPhotoId, amtLimit)
    }

    suspend fun getAllEventsPaginated(lastEventDate: OffsetDateTime, lastEventId: Long, amtLimit: Int): List<Event> = withContext(Dispatchers.IO) {
        eventDao.getAllEventsPaginated(lastEventDate, lastEventId, amtLimit)
    }

    suspend fun getPetsOfEventPaginated(eventId: Long, lastPetId: Long, petsAmt: Int): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        eventDao.getPetsOfEventPaginated(eventId, lastPetId, petsAmt)
    }

    suspend fun getAllTagsOfEventAlphabeticalOrder(eventId: Long): List<Tag> = withContext(Dispatchers.IO) {
        eventDao.getAllTagsOfEventAlphabeticalOrder(eventId)
    }

    suspend fun getTagsOfEvent(eventId: Long): List<Tag> = withContext(Dispatchers.IO) {
        eventDao.getAllTagsOfEvent(eventId)
    }

}