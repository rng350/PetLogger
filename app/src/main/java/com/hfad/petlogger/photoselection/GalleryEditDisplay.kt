package com.hfad.petlogger.photoselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.hfad.petlogger.*
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.databinding.FragmentGalleryEditDisplayBinding
import com.hfad.petlogger.recyclerviews.ItemPickers

class GalleryEditDisplay(
    val binding: FragmentGalleryEditDisplayBinding,
    val photoDao: PhotoDao,
    val eventDao: EventDao,
    private val parentFragment: Fragment,
    private val associatedID: MutableLiveData<Long>,
    private val galleryEditDisplayViewModel: GalleryEditDisplayViewModel,
    private val galleryViewModel: GalleryViewModel
) {
    private var _galleryPicker: GalleryPicker? = null
    private val galleryPicker get() = _galleryPicker!!

    fun onCreate(savedInstanceState: Bundle?) {
        ItemPickers.setupOldPhotoDeselector(
            galleryEditDisplayViewModel.oldPhotosAssociated,
            binding.oldPhotosSelection,
            galleryEditDisplayViewModel.oldPhotosAssociatedTracker,
            parentFragment.viewLifecycleOwner,
            parentFragment.requireContext())
        ItemPickers.setupNewPhotoDeselector(
            binding.newPhotosSelection,
            galleryEditDisplayViewModel.newPhotosAssociatedTracker,
            parentFragment.viewLifecycleOwner,
            parentFragment.requireContext())

        associatedID.observeOnce(parentFragment.viewLifecycleOwner, Observer {
            it?.let {
                galleryEditDisplayViewModel.initOldPhotosAssociated()
            }
        })

        galleryEditDisplayViewModel.oldPhotosAssociated.observeOnce(parentFragment.viewLifecycleOwner, Observer {
            galleryEditDisplayViewModel.setupSharedCounterSize()
        })

        _galleryPicker = GalleryPicker(parentFragment, binding.galleryPicker, galleryViewModel, associatedID = associatedID)
        galleryPicker.onCreate(savedInstanceState)
    }

    fun onResume() {
        galleryPicker.onResume()
    }

    fun onDestroy() {
        galleryPicker.onDestroy()
    }
}