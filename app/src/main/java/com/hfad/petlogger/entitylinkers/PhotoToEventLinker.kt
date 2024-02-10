package com.hfad.petlogger.entitylinkers

import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.entities.PhotoEvent

class PhotoToEventLinker(private val dao: PhotoDao): EntityLinker {
    override suspend fun associateWith(photoID: Long, eventID: Long) {
        dao.insert(PhotoEvent(photoID, eventID))
        //Log.d("inserted_link", "photoID: ${photoID}, eventID: ${eventID}")
    }
}