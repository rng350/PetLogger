package com.hfad.petlogger.screens.sections.associatedentities

import RecyclerViewPaginator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentFullGalleryBinding
import com.hfad.petlogger.screens.photo.FullGalleryViewModel
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedPhotosDisplayUseCase

class AssociatedPhotosDisplayFragment : Fragment() {
    private var _binding: FragmentFullGalleryBinding? = null
    val binding: FragmentFullGalleryBinding get() = _binding!!
    private val galleryViewModel: FullGalleryViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFullGalleryBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = galleryViewModel

        binding.mediaListTopAppBarLayout.visibility = View.GONE

        SetupAssociatedPhotosDisplayUseCase(
            galleryViewModel.photos,
            galleryViewModel.photoNavigator,
            binding.gallery,
            requireContext(),
            lifecycleScope,
            viewLifecycleOwner
        )()

        //binding.gallery.addItemDecoration(PhotoItemSpacingDecoration())

        RecyclerViewPaginator(
            recyclerView = binding.gallery,
            isLoading = {galleryViewModel.isLoading()},
            loadMore = {galleryViewModel.load()},
            onLast = {galleryViewModel.onLastPage()}
        )

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                galleryViewModel.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                galleryViewModel.onQueryTextChanged(newText)
                return true
            }
        })

        binding.addPhotoButton.setOnClickListener {
            galleryViewModel.newPhotoNavigator.navigateToNewEntityScreen()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.gallery.adapter = null
        _binding = null
    }
}