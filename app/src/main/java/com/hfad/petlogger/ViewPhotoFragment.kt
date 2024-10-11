package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentViewPhotoBinding
import com.hfad.petlogger.photodisplay.stateless.GetMoreEventsOfPhotoUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreNotesOfPhotoUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMorePetsOfPhotoUseCase
import com.hfad.petlogger.repositories.MediaRepository

class ViewPhotoFragment : Fragment() {
    private var _binding: FragmentViewPhotoBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPhotoBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val photoId = ViewPhotoFragmentArgs.fromBundle(requireArguments()).photoId
        val viewPhotoViewModel = ViewModelProvider(this, ViewPhotoViewModel.provideFactory(mediaRepository, photoId)).get(ViewPhotoViewModel::class.java)
        binding.viewPhotoViewModel = viewPhotoViewModel

        setAppBarTitle(getString(R.string.viewing_photo_details))

        viewPhotoViewModel.photo.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.photoDisplay)
            } else binding.photoDisplay.setImageResource(R.drawable.placeholder)
        }

        val getPetsOfPhotoForDisplayUseCase = GetMorePetsOfPhotoUseCase(mediaRepository, photoId, petsAmt = 10)
        val associatedPetsDisplayViewModel = ViewModelProvider(this, AssociatedPetsDisplayViewModel.provideFactory(getPetsOfPhotoForDisplayUseCase)).get(AssociatedPetsDisplayViewModel::class.java)
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        val getEventsOfPhotoForDisplayUseCase = GetMoreEventsOfPhotoUseCase(mediaRepository, photoId, eventAmt = 10)
        val associatedEventsDisplayViewModel = ViewModelProvider(this, AssociatedEventsDisplayViewModel.provideFactory(getEventsOfPhotoForDisplayUseCase)).get(AssociatedEventsDisplayViewModel::class.java)
        binding.associatedEventsDisplayViewModel = associatedEventsDisplayViewModel

        val getNotesOfPhoto = GetMoreNotesOfPhotoUseCase(mediaRepository, photoId, notesAmt = 10)
        val associatedNotesDisplayViewModel = ViewModelProvider(this, AssociatedNotesDisplayViewModel.provideFactory(getNotesOfPhoto)).get(AssociatedNotesDisplayViewModel::class.java)
        binding.associatedNotesDisplayViewModel = associatedNotesDisplayViewModel

        binding.editButton.setOnClickListener{
            findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToEditPhotoFragment(photoId))
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}