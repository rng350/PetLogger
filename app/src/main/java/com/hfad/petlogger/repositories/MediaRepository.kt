package com.hfad.petlogger.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.entities.PetProfilePhoto
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoEvent
import com.hfad.petlogger.entities.PhotoNote
import com.hfad.petlogger.entitylinkers.EntityLinker
import com.hfad.petlogger.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

class MediaRepository(
    private val photoDao: PhotoDao,
    private val context: Context
) {
    suspend fun getEventPhotos(eventID: Long): List<Photo> = withContext(Dispatchers.IO) {
        photoDao.fetchPhotosOfEvent(eventID)
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

    suspend fun addPhoto(photo: Photo): Photo? = withContext(Dispatchers.IO) {
            // step 1 save photo to file storage
        val submittedPhotoDeffered = async {
            saveToLocalStorage(photo)
        }.await()
        // step 2 save new photo to database
        submittedPhotoDeffered?.let {
            photoDao.insert(it)
        }
        submittedPhotoDeffered
    }

    suspend fun addEventPhoto(photo: Photo, eventID: Long) {
        withContext(Dispatchers.IO) {
            val photoAdded = async {
                addPhoto(photo)
            }.await()
            photoAdded?.let {
                photoDao.insert(PhotoEvent(it.id, eventID))
            }
        }
    }

    suspend fun addPetPhoto(photo: Photo, petID: Long) {
        withContext(Dispatchers.IO) {
            val photoAdded = async {
                addPhoto(photo)
            }.await()
            photoAdded?.let {
                photoDao.insert(PetProfilePhoto(photo.id, petID))
            }
        }
    }

    suspend fun addNotePhoto(photo: Photo, noteId: Long) {
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
            deleteFromLocalStorage(photo.name)
        }
    }

    // TODO: Implement the following check
    // 1. check that there's enough space
    // 2a. if so, try to save files
    // 2b. if not, create a toaster saying there's not enough space
    private suspend fun saveToLocalStorage(photo: Photo): Photo? = withContext(Dispatchers.IO) {
        val fileName = generateFilename(photo.date)
        var height = 0
        var width = 0

        context.contentResolver.openInputStream(photo.contentUri).use { input ->
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                val options = BitmapFactory.Options()
                BitmapFactory.decodeStream(input, null, options)!!.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    output
                )
                height = options.outHeight
                width = options.outWidth
            }
        }

        val createdFile = File(context.filesDir, fileName)
        if (createdFile.exists()) {
            val fileSize = createdFile.size
            return@withContext Photo(photo.id, fileName, createdFile.toUri(), width, height, fileSize, photo.date)
        }
        return@withContext null
    }

    // filename format
    // [year][month][day]_[hour]h[minute]m[second]s_[uuid]
    // i.e.
    // 20220614_18h22m_[random UUID]
    // 00000000_00h00m_[random UUID]
    private fun generateFilename(date: LocalDateTime?): String {
        var prefix = "00000000_00h00m"

        date?.let {
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

    private suspend fun deleteFromLocalStorage(filename: String) {
        val file = File(context.filesDir, filename)
        if (file.exists()) file.delete()
    }
}