package com.hfad.petlogger.screens.photo

import RecyclerViewPaginator
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentFullGalleryBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.photos.usecases.GetMoreOfAllPhotosUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedPhotosDisplayUseCase

class FullGalleryFragment : Fragment() {
    private var _binding: FragmentFullGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FullGalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFullGalleryBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val getAllPhotos = GetMoreOfAllPhotosUseCase(mediaRepository, photosAmt = 18)
        viewModel = ViewModelProvider(this, FullGalleryViewModel.provideFactory(getAllPhotos)).get(
            FullGalleryViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setAppBarTitle(getString(R.string.media_gallery_header))

        SetupAssociatedPhotosDisplayUseCase(
            photos = viewModel.photos,
            photoNavigator = viewModel.photoNavigator,
            recyclerView = binding.gallery,
            context = application.applicationContext,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

        RecyclerViewPaginator(
            recyclerView = binding.gallery,
            loadMore = {viewModel.load()},
            isLoading = {viewModel.isLoading()},
            onLast = {viewModel.onLastPage()}
        )

        binding.addPhotoButton.setOnClickListener {
            this.findNavController().navigateSafe(FullGalleryFragmentDirections.actionFullGalleryFragmentToNewPhotoFragment())
        }
        viewModel.photoNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = FullGalleryFragmentDirections.actionFullGalleryFragmentToViewPhotoFragment(it)
                this.findNavController().navigateSafe(action)
                viewModel.photoNavigator.onNavigated()
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.gallery.adapter = null
        _binding = null
    }
}