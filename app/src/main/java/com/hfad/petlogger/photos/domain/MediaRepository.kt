package com.hfad.petlogger.photos.domain

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.common.associationentities.PetPhoto
import com.hfad.petlogger.common.associationentities.PhotoEvent
import com.hfad.petlogger.common.associationentities.PhotoNote
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.domain.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

class MediaRepository(
    private val database: PetLoggerDatabase,
    private val context: Context
) {
    private val photoDao = database.photoDao
    private val noteDao = database.noteDao

    suspend fun getPhoto(photoId: Long): Photo? = withContext(Dispatchers.IO) {
        photoDao.getPhoto(photoId)
    }

    suspend fun updatePhoto(
        photo: Photo,
        petsToAdd: List<Long> = listOf<Long>(),
        petsToRemove: List<Long> = listOf<Long>(),
        eventsToAdd: List<Long> = listOf<Long>(),
        eventsToRemove: List<Long> = listOf<Long>(),
        notesToAdd: List<Note> = listOf<Note>(),
        notesToRemove: List<Note> = listOf<Note>(),
        notesToUpdate: List<Note> = listOf<Note>(),
        tagsToAdd: List<Tag> = listOf<Tag>(),
        tagsToRemove: List<Tag> = listOf<Tag>()
    ) = withContext(Dispatchers.IO) {
        val updatePhoto = async {
            photoDao.update(photo)
        }
        val addPets = petsToAdd.map { petId ->
            async {
                photoDao.associate(PetPhoto(petId = petId, photoId = photo.id))
            }
        }
        val removePets = petsToRemove.map { petId ->
            async {
                photoDao.dissociate(PetPhoto(petId = petId, photoId = photo.id))
            }
        }
        val addEvents = eventsToAdd.map { eventId ->
            async {
                photoDao.insert(PhotoEvent(eventID = eventId, photoID = photo.id))
            }
        }
        val removeEvents = eventsToRemove.map { eventId ->
            async {
                photoDao.delete(PhotoEvent(eventID = eventId, photoID = photo.id))
            }
        }
        val addNotes = notesToAdd.map { note ->
            async {
                Log.d("updatePhoto", "added note: $note")
                photoDao.insert(PhotoNote(noteId = note.id, photoId = photo.id))
            }
        }
        val removeNotes = notesToRemove.map { note ->
            async {
                photoDao.delete(PhotoNote(noteId = note.id, photoId = photo.id))
            }
        }
        val updateNotes = notesToUpdate.map { note ->
            async {
                noteDao.update(note)
            }
        }
        val tagRepository = TagRepository(database)
        val tagsAttached = tagsToAdd.map { tag ->
            async {
                attachPhotoToTag(tagRepository, photo.id, tag)
            }
        }
        val tagsDetached = tagsToRemove.map { tag ->
            async {
                tagRepository.detachPhotoFromTag(photoId = photo.id, tag)
            }
        }
        updatePhoto.await()
        addPets.awaitAll()
        removePets.awaitAll()
        addEvents.awaitAll()
        removeEvents.awaitAll()
        addNotes.awaitAll()
        removeNotes.awaitAll()
        updateNotes.awaitAll()
        tagsAttached.awaitAll()
        tagsDetached.awaitAll()
    }

    suspend fun updateEventPhotos(eventID: Long, photosToAdd: List<Photo>, photosToDelete: List<Photo>) {
        withContext(Dispatchers.IO) {
            for (photoToAdd in photosToAdd) {
                launch {
                    photoDao.insert(PhotoEvent(photoToAdd.id, eventID))
                }
            }
            for (photoToDelete in photosToDelete) {
                launch {
                    photoDao.delete(PhotoEvent(photoToDelete.id, eventID))
                }
            }
        }
    }

    // Inserting a 'stand-alone' photo (from the NewPhoto screen)
    suspend fun insertNewPhoto(
        photo: Photo,
        newAttachedNotes: List<Note> = listOf<Note>(),
        existingAttachedNotes: List<Note> = listOf<Note>(),
        pets: List<Long> = listOf<Long>(),
        events: List<Long> = listOf<Long>(),
        tags: List<Tag> = listOf<Tag>()
    ): Photo? = withContext(Dispatchers.IO) {
        val photoAdded = addPhoto(photo)
        photoAdded?.let {
            val existingNotesAttached = existingAttachedNotes.map { note ->
                async {
                    noteDao.attachPhoto(PhotoNote(photoId=photoAdded.id, noteId = note.id))
                }
            }
            val petDao = database.petDao
            val petsAttached = pets.map { petId ->
                async {
                    petDao.insertPetPhoto(PetPhoto(petId = petId, photoId = photoAdded.id))
                }
            }
            val eventsAttached = events.map { eventId ->
                async {
                    photoDao.insert(PhotoEvent(photoID = photoAdded.id, eventID = eventId))
                }
            }
            val tagRepository = TagRepository(database)
            val tagsAttached = tags.map { tag ->
                async {
                    attachPhotoToTag(tagRepository, photoId = photoAdded.id, tag)
                }
            }
            existingNotesAttached.awaitAll()
            petsAttached.awaitAll()
            eventsAttached.awaitAll()
            tagsAttached.awaitAll()
        }
        photoAdded
    }

    // Inserting a photo as an attachment
    suspend fun addPhoto(photo: Photo): Photo? = withContext(Dispatchers.IO) {
        // Step 1: Check that photo doesn't already exist first
        Log.d("MedRep:addPhoto", "BEFORE")
        val checkPhoto = photoDao.checkPhoto(photo.id)
        checkPhoto?.let {
            Log.d("MedRep:addPhoto", "Photo already in gallery")
            return@withContext it
        }

        Log.d("MedRep:addPhoto", "Photo not found in gallery")
        // step 2 save photo to file storage
        val submittedPhotoDeferred = async {
            saveToLocalStorage(photo)
        }.await()

        Log.d("MedRep:addPhoto", "Photo saved to file storage... ${submittedPhotoDeferred}")
        // step 3 save new photo to database
        submittedPhotoDeferred?.let {
            photoDao.insert(it)
        }
        Log.d("MedRep:addPhoto", "Photo inserted in DB... ${submittedPhotoDeferred}")
        submittedPhotoDeferred
    }

    suspend fun addNewPhotosForEvent(photos: List<Photo>, eventId: Long) = withContext(Dispatchers.IO) {
        photos.map {photo ->
            async {
                addNewPhotoForEvent(photo, eventId)
            }
        }.awaitAll()
    }

    suspend fun addNewPhotoForEvent(photo: Photo, eventID: Long) {
        withContext(Dispatchers.IO) {
            val photoAdded = async {
                addPhoto(photo)
            }.await()
            photoAdded?.let {
                photoDao.insert(PhotoEvent(it.id, eventID))
            }
        }
    }

    suspend fun addNewPhotoForNote(photo: Photo, noteId: Long) {
        withContext(Dispatchers.IO) {
            val photoAdded = async {
                addPhoto(photo)
            }.await()
            photoAdded?.let {
                photoDao.insert(PhotoNote(photo.id, noteId))
            }
        }
    }

    suspend fun delete(photo: Photo) {
        withContext(Dispatchers.IO) {
            // step 1 delete photo from database
            val photoDeletedDeferred = async {
                photoDao.delete(photo)
            }
            // step 2 delete photo from file storage
            photoDeletedDeferred.await()
            deleteFromLocalStorage(photo.filename)
        }
    }

    suspend fun retrievePhotos(context: Context, uris: List<Uri>): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<Photo>()
        for (uri in uris) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED
            )
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val fileSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                while(cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val size = cursor.getDouble(fileSizeColumn)
                    val date: OffsetDateTime =
                        if (dateTakenColumn != -1) {
                            OffsetDateTime.ofInstant(Instant.ofEpochMilli(cursor.getLong(dateTakenColumn)), ZoneId.of("UTC"))
                        } else if (dateAddedColumn != -1) {
                            OffsetDateTime.ofInstant(Instant.ofEpochMilli(cursor.getLong(dateAddedColumn)), ZoneId.of("UTC"))
                        } else OffsetDateTime.now()
                    photos.add(Photo(id, "", displayName, contentUri, width, height, size, date))
                }
            }
        }
        Log.d("MediaRep", "Retrieved Photos: ${photos.toString()}")
        photos.toList()
    }

    // TODO: Implement the following check
    // 1. check that there's enough space
    // 2a. if so, try to save files
    // 2b. if not, create a toast saying there's not enough space
    private suspend fun saveToLocalStorage(photo: Photo): Photo? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(photo.contentUri) ?: return@withContext null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val scaledInputStream = context.contentResolver.openInputStream(photo.contentUri) ?: return@withContext null
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeStream(scaledInputStream, null, options)
            scaledInputStream.close()

            if (bitmap == null) return@withContext null

            val tempFile = File(context.cacheDir, "temp_${photo.filename}_${UUID.randomUUID()}")
            context.contentResolver.openInputStream(photo.contentUri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            val exif = ExifInterface(tempFile.absolutePath)
            val rotationAngle = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            val correctedBitmap = if (rotationAngle != 0) {
                val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val fileName = generateFilename(photo.date)
            val file = File(context.filesDir, fileName)
            file.outputStream().use { output ->
                correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            }

            correctedBitmap.recycle()
            tempFile.delete()

            val fileSize = file.length().toDouble()
            return@withContext Photo(
                photo.id,
                photo.title,
                fileName,
                file.toUri(),
                correctedBitmap.width,
                correctedBitmap.height,
                fileSize,
                photo.date
            )
        } catch (e: Exception) {
            Log.e("MediaRepository", "${e.printStackTrace()}")
            return@withContext null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > maxHeight || width > maxWidth) {
            val heightRatio = Math.round(height.toFloat() / maxHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / maxWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }

        return inSampleSize
    }

    // filename format
    // [year][month][day]_[hour]h[minute]m[second]s_[uuid]
    // i.e.
    // 20220614_18h22m_[random UUID]
    // 00000000_00h00m_[random UUID]
    private fun generateFilename(date: OffsetDateTime?): String {
        val dateLocal = date?.toLocalDateTime()
        var prefix = "00000000_00h00m"

        dateLocal?.let {
            val yTho = it.year / 1000
            val yHun = (it.year % 1000) / 100
            val yTen = (it.year % 100) / 10
            val yOne = it.year % 10

            val monTen = it.month.value / 10
            val monOne = it.month.value % 10

            val dTen = it.dayOfMonth / 10
            val dOne = it.dayOfMonth % 10

            val hTen = it.hour / 10
            val hOne = it.hour % 10

            val minTen = it.minute / 10
            val minOne = it.minute % 10
            prefix = "${yTho}${yHun}${yTen}${yOne}${monTen}${monOne}${dTen}${dOne}_${hTen}${hOne}h${minTen}${minOne}m"
        }

        var fileName = "${prefix}_${UUID.randomUUID()}"
        var hasUnusedFilename = false

        // check if filename is unused
        do {
            if (fileAlreadyExists(fileName))
                fileName = "${prefix}_${UUID.randomUUID()}"
            else hasUnusedFilename = true
        } while (!hasUnusedFilename)

        return fileName
    }

    private fun fileAlreadyExists(fileName: String): Boolean {
        return File("${context.filesDir}/${fileName}").exists()
    }

    private suspend fun deleteFromLocalStorage(filename: String) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, filename)
        if (file.exists()) file.delete()
    }

    suspend fun getPetsOfPhoto(photoId: Long): List<PetWithProfilePic> = withContext(Dispatchers.IO){
        photoDao.getPetsOfPhoto(photoId)
    }

    suspend fun getEventsOfPhoto(photoId: Long): List<Event> = withContext(Dispatchers.IO) {
        photoDao.getEventsOfPhoto(photoId)
    }

    suspend fun getEventsOfPhotoPaginated(
        photoId: Long,
        lastEventDate: OffsetDateTime,
        lastEventId: Long,
        eventAmt: Int
    ): List<Event> = withContext(Dispatchers.IO) {
        photoDao.getEventsOfPhotoPaginated(photoId, lastEventDate, lastEventId, eventAmt)
    }

    suspend fun getNotesOfPhotoPaginated(
        photoId: Long,
        lastNoteEditedDate: OffsetDateTime,
        lastNoteId: Long,
        notesAmt: Int)
    : List<Note> = withContext(Dispatchers.IO) {
        photoDao.getNotesOfPhotoPaginated(photoId, lastNoteEditedDate, lastNoteId, notesAmt)
    }

    suspend fun getAllPhotosPaginated(lastPhotoDate: OffsetDateTime, lastPhotoId: Long, photosAmt: Int): List<Photo> = withContext(Dispatchers.IO) {
        photoDao.getAllPhotosPaginated(lastPhotoDate, lastPhotoId, photosAmt)
    }

    suspend fun getPetsOfPhotoPaginated(photoId: Long, lastPetId: Long, petsAmt: Int): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        photoDao.getPetsOfPhotoPaginated(photoId, lastPetId, petsAmt)
    }

    suspend fun getNotesOfPhoto(photoId: Long): List<Note> = withContext(Dispatchers.IO) {
        photoDao.getNotesOfPhoto(photoId)
    }

    private suspend fun attachPhotoToTag(tagRepository: TagRepository, photoId: Long, tag: Tag) {
        if (tag.tagId == Constants.newTagPlaceholderId) {
            tagRepository.attachPhotoToNewTag(photoId, tag)
        } else tagRepository.attachPhotoToExistingTag(photoId, tag)
    }

    suspend fun getTagsOfPhoto(photoId: Long): List<Tag> = withContext(Dispatchers.IO) {
        photoDao.getTagsOfPhoto(photoId)
    }

    suspend fun getTagsOfPhotoAlphabeticalOrder(photoId: Long): List<Tag> = withContext(Dispatchers.IO) {
        photoDao.getTagsOfPhotoAlphabeticalOrder(photoId)
    }

}