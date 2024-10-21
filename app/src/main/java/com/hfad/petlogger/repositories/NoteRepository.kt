package com.hfad.petlogger.repositories

import androidx.room.withTransaction
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.entities.EventNote
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetNote
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoNote
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightForListFetched
import com.hfad.petlogger.entities.WeightNote
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class NoteRepository(
    private val database: PetLoggerDatabase,
    private val mediaRepository: MediaRepository
) {
    private val noteDao = database.noteDao
    suspend fun getNote(noteId: Long): Note
    = withContext(Dispatchers.IO) {
        noteDao.get(noteId)
    }

    suspend fun getPetsOfNote(noteId: Long): List<Pet>
    = withContext(Dispatchers.IO) {
        noteDao.getPetsOfNote(noteId)
    }

    suspend fun getAllNotes(): List<Note> {
        return noteDao.getAll()
    }

    suspend fun insertNote(note: Note,
                           pets: List<Long> = listOf<Long>(),
                           events: List<Event> = listOf<Event>(),
                           weights: List<Weight> = listOf<Weight>(),
                           photos: List<Photo> = listOf<Photo>()): Long
    = withContext(Dispatchers.IO) {

        val noteId = database.withTransaction {
            noteDao.insert(note)
        }

        val petsDeferred = pets.map {
            async {
                insertPetNote(noteId, it)
            }
        }
        val eventsDeferred = events.map {
            async {
                insertEventNote(noteId, it.eventId)
            }
        }
        val weightsDeferred = weights.map {
            async {
                insertWeightNote(noteId, it.id)
            }
        }
        val photosDeferred = photos.map {
            async {
                mediaRepository.addNewPhotoForNote(it, noteId)
            }
        }

        petsDeferred.awaitAll()
        eventsDeferred.awaitAll()
        weightsDeferred.awaitAll()
        photosDeferred.awaitAll()

        noteId
    }

    suspend fun updateNote(note: Note,
                           petsToAdd: List<Long> = listOf<Long>(),
                           petsToRemove: List<Long> = listOf<Long>(),
                           eventsToAdd: List<Event> = listOf<Event>(),
                           eventsToRemove: List<Event> = listOf<Event>(),
                           weightsToAdd: List<Weight> = listOf<Weight>(),
                           weightsToRemove: List<Weight> = listOf<Weight>(),
                           photosToAdd: List<Photo> = listOf<Photo>(),
                           photosToRemove: List<Photo> = listOf<Photo>())
    = withContext(Dispatchers.IO) {
        val noteUpdated = async {
            noteDao.update(note)
        }
        val petsAttached = async {
            noteDao.attachPets(petsToAdd.map{ petID -> PetNote(petId=petID, noteId=note.id)})
        }
        val petsDetached = async {
            noteDao.detachPets(petsToRemove.map{ petID -> PetNote(petId=petID, noteId=note.id)})
        }
        val eventsAttached = async {
            noteDao.attachEvents(eventsToAdd.map{ event -> EventNote(eventId = event.eventId, noteId = note.id)})
        }
        val eventsDetached = async {
            noteDao.detachEvents(eventsToRemove.map{ event -> EventNote(eventId = event.eventId, noteId = note.id)})
        }
        val weightsAttached = async {
            noteDao.attachWeights(weightsToAdd.map{ weight -> WeightNote(weightId=weight.id, noteId=note.id)})
        }
        val weightsDetached = async {
            noteDao.detachWeights(weightsToRemove.map{ weight -> WeightNote(weightId=weight.id, noteId=note.id)})
        }
        val photosAttached = photosToAdd.map { newPhoto ->
            async {
                mediaRepository.addNewPhotoForNote(newPhoto, note.id)
            }
        }
        val photosDetached = async {
            noteDao.detachPhotos(photosToRemove.map{ photo -> PhotoNote(photoId = photo.id, noteId=note.id)})
        }
        noteUpdated.await()
        petsAttached.await()
        petsDetached.await()
        eventsAttached.await()
        eventsDetached.await()
        weightsAttached.await()
        weightsDetached.await()
        photosDetached.await()
        photosAttached.awaitAll()
    }

    suspend fun insertPetNote(noteId: Long, petId: Long) {
        withContext(Dispatchers.IO) {
            noteDao.attachPet(PetNote(petId, noteId))
        }
    }

    suspend fun insertEventNote(noteId: Long, eventId: Long) {
        withContext(Dispatchers.IO) {
            noteDao.attachEvent(EventNote(eventId, noteId))
        }
    }

    suspend fun insertWeightNote(noteId: Long, weightId: Long) {
        withContext(Dispatchers.IO) {
            noteDao.attachWeight(WeightNote(weightId, noteId))
        }
    }

    suspend fun insertPhotoNote(noteId: Long, photoId: Long) = withContext(Dispatchers.IO){
        noteDao.attachPhoto(PhotoNote(photoId, noteId))
    }

    fun getPhotosOfNoteAsFlow(noteId: Long): Flow<List<Photo>> {
        return noteDao.getPhotosOfNote(noteId)
    }

    fun getPetsWithProfilePicsOfNoteAsFlow(noteId: Long): Flow<List<PetWithProfilePic>> {
        return noteDao.getPetsWithProfilePicOfNoteAsFlow(noteId)
    }

    suspend fun getEventsOfNote(noteId: Long): List<Event> = withContext(Dispatchers.IO) {
        noteDao.getEventsOfNote(noteId)
    }

    suspend fun getPetsWithProfilePicsOfNote(noteId: Long): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        noteDao.getPetsWithProfilePicOfNote(noteId)
    }

    suspend fun getPhotosOfNote(noteId: Long): List<Photo> = withContext(Dispatchers.IO) {
        noteDao.getPhotosOfNoteAsList(noteId)
    }

    suspend fun getPhotosOfNotePaginated(noteId: Long, lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo> = withContext(Dispatchers.IO) {
        noteDao.getPhotosOfNotePaginated(noteId, lastPhotoDate, lastPhotoId, amtLimit)
    }

    suspend fun delete(note: Note) = withContext(Dispatchers.IO) {
        noteDao.delete(note)
    }

    suspend fun getWeightsOfNote(noteId: Long): List<WeightWithPetName> = withContext(Dispatchers.IO) {
        noteDao.getWeightsOfNote(noteId)
            .map{WeightWithPetName(weight=it.weight, petName=it.assocPet.petName)}
            .sortedByDescending { it.weight.weightDateTime }
    }

    fun getEventsOfNoteAsFlow(noteId: Long): Flow<List<EventForList>> {
        val getDateDisplayUseCase = GetDateDisplayUseCase()
        val getTimeDisplayUseCase = GetTimeDisplayUseCase()
        return noteDao
            .getEventsOfNoteAsFlow(noteId)
            .map { it
                .sortedByDescending { event -> event.date }
                .map { event -> EventForList(
                    eventId = event.eventId,
                    eventDate = getDateDisplayUseCase(event.date),
                    eventTime = getTimeDisplayUseCase(event.date),
                    eventTitle = event.title) }
            }.flowOn(Dispatchers.IO)
    }

    fun getAllNotesAsFlow(): Flow<List<Note>> {
        return noteDao.getAllNotesAsFlow()
    }

    suspend fun getNoteEventsAsListPaginated(
        noteId: Long,
        lastEventDate: OffsetDateTime = OffsetDateTime.MAX,
        lastEventId: Long = Long.MAX_VALUE,
        eventAmt: Int
    ): List<Event> = withContext(Dispatchers.IO) {
        noteDao.getEventsOfNotePaginated(noteId, lastEventDate, lastEventId, eventAmt)
    }

    suspend fun getAllNotesPaginated(lastNoteUpdateDate: OffsetDateTime, lastNoteId: Long, noteAmt: Int): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getAllNotesPaginated(lastNoteUpdateDate, lastNoteId, noteAmt)
    }

    suspend fun getWeightsOfNotePaginated(noteId: Long, lastWeightDateTime: OffsetDateTime, lastWeightId: Long, weightsAmt: Int): List<WeightForListFetched> = withContext(Dispatchers.IO) {
        noteDao.getWeightsOfNotePaginated(noteId, lastWeightDateTime, lastWeightId, weightsAmt)
    }

    suspend fun getPetsOfNotePaginated(noteId: Long, lastPetId: Long, petsAmt: Int): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        noteDao.getPetsOfNotePaginated(noteId, lastPetId, petsAmt)
    }

    suspend fun getSearchedNotesFromAllPaginated(query: String, lastNoteUpdateDate: OffsetDateTime, lastNoteId: Long, noteAmt: Int): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getSearchedNotesFromAllPaginated(query, lastNoteUpdateDate, lastNoteId, noteAmt)
    }
}