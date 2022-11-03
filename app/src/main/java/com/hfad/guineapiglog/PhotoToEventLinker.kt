package com.hfad.guineapiglog

import android.util.Log

class PhotoToEventLinker(private val dao: PhotoDao): EntityLinker {
    override suspend fun associateWith(photoID: Long, eventID: Long) {
        dao.insert(PhotoEvent(photoID, eventID))
        Log.d("inserted_link", "photoID: ${photoID}, eventID: ${eventID}")
    }
}