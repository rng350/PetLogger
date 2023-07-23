package com.hfad.guineapiglog.entitylinkers

import com.hfad.guineapiglog.dao.PhotoDao
import com.hfad.guineapiglog.entities.PhotoEvent

class PhotoToEventLinker(private val dao: PhotoDao): EntityLinker {
    override suspend fun associateWith(photoID: Long, eventID: Long) {
        dao.insert(PhotoEvent(photoID, eventID))
        //Log.d("inserted_link", "photoID: ${photoID}, eventID: ${eventID}")
    }
}