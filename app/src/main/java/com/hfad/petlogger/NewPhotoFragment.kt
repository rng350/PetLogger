package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentNewNoteBinding
import com.hfad.petlogger.databinding.FragmentNewPhotoBinding
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository

class NewPhotoFragment : Fragment() {
    private var _binding: FragmentNewPhotoBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPhotoBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, requireContext())
        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(database.petDao, mediaRepository)
        val eventRepository = EventRepository(database, mediaRepository)
        val weightRepository = WeightRepository(database)

        val newPhotoViewModel = ViewModelProvider(this, NewPhotoViewModel.provideFactory(mediaRepository)).get(NewPhotoViewModel::class.java)
        val petSelectorViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(petRepository)).get(PetMultiSelectionViewModel::class.java)
        val eventSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository)).get(EventMultiSelectionViewModel::class.java)
        val weightMultiSelectionViewModel = ViewModelProvider(this, WeightMultiSelectionViewModel.provideFactory(weightRepository)).get(WeightMultiSelectionViewModel::class.java)
        binding.newPhotoViewModel = newPhotoViewModel
        binding.petSelectorViewModel = petSelectorViewModel
        binding.eventSelectorViewModel = eventSelectionViewModel
        binding.weightSelectorViewModel = weightMultiSelectionViewModel

        newPhotoViewModel.photo.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.photoDisplay)
            } else binding.photoDisplay.setImageResource(R.drawable.placeholder)
        }

        val pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    newPhotoViewModel.setPhoto(requireContext(), uri)
                }
            }

        binding.selectPhotoButton.setOnClickListener{
            pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.clearPhotoSelectionButton.setOnClickListener{
            newPhotoViewModel.resetPhotoSelection()
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.clearButton.setOnClickListener{
            newPhotoViewModel.resetPhotoSelection()
        }

        binding.submitButton.setOnClickListener{
            newPhotoViewModel.submit()
        }

        newPhotoViewModel.goBack.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigate(R.id.action_newPhotoFragment_to_fullGalleryFragment)
            }
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}