package com.hfad.guineapiglog.photoselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.guineapiglog.entitylinkers.EntityLinker
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.PhotoDao
import com.hfad.guineapiglog.selectiontracker.SelectionTracker

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