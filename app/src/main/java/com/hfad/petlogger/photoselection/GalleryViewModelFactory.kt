package com.hfad.petlogger.photoselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.entitylinkers.EntityLinker
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.selectiontracker.SelectionTracker

class GalleryViewModelFactory(private val entityLinker: EntityLinker,
                              private val photoDao: PhotoDao,
                              private val photosSelected: SelectionTracker<Photo>
                              ) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            return GalleryViewModel(entityLinker, photoDao, photosSelected) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}