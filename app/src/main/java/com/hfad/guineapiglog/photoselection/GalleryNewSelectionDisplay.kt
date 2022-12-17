package com.hfad.guineapiglog.photoselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.hfad.guineapiglog.R

// displays photos that have been picked, for use in conjunction with gallery picker
class GalleryNewSelectionDisplay : Fragment() {
    private val viewModel: GalleryViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.hasExternalReadPermission.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                // TODO: implement
            }
        })
        return inflater.inflate(R.layout.fragment_gallery_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun loadPhotoSelection() {
        // is permission granted?
        // if so, display photos
        // if not, put up a note saying permission is needed
    }
}