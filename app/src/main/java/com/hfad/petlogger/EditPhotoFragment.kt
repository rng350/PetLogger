package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditPhotoBinding
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository

class EditPhotoFragment : Fragment() {

    private var _binding: FragmentEditPhotoBinding? = null
    private val binding get() = _binding!!
    private lateinit var editPhotoViewModel: EditPhotoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPhotoBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, requireContext())
        val photoId = EditPhotoFragmentArgs.fromBundle(requireArguments()).photoId

        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(database, mediaRepository)
        val eventRepository = EventRepository(database, mediaRepository)
        val weightRepository = WeightRepository(database)

        val petSelectorViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(petRepository)).get(PetMultiSelectionViewModel::class.java)
        val eventSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository)).get(EventMultiSelectionViewModel::class.java)
        val weightMultiSelectionViewModel = ViewModelProvider(this, WeightMultiSelectionViewModel.provideFactory(weightRepository)).get(WeightMultiSelectionViewModel::class.java)

        editPhotoViewModel = ViewModelProvider(this, EditPhotoViewModel.provideFactory(mediaRepository, photoId)).get(EditPhotoViewModel::class.java)
        binding.editPhotoViewModel = editPhotoViewModel
        binding.petSelectorViewModel = petSelectorViewModel
        binding.eventSelectorViewModel = eventSelectionViewModel
        binding.weightSelectorViewModel = weightMultiSelectionViewModel

        setAppBarTitle(getString(R.string.editing_photo_details))

        binding.submitButton.setOnClickListener{
            editPhotoViewModel.submit(
                petsToAdd = petSelectorViewModel.getPetsToAdd(),
                petsToRemove = petSelectorViewModel.getPetsToRemove(),
                eventsToAdd = eventSelectionViewModel.getEventsToAdd(),
                eventsToRemove = eventSelectionViewModel.getEventsToRemove(),
                weightsToAdd = weightMultiSelectionViewModel.getWeightsToAdd(),
                weightsToRemove = weightMultiSelectionViewModel.getWeightsToRemove()
            )
        }

        binding.deleteButton.setOnClickListener{
            editPhotoViewModel.deletePhoto()
        }

        binding.resetButton.setOnClickListener{
            editPhotoViewModel.reset()
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        editPhotoViewModel.goBack.observe(viewLifecycleOwner, Observer {shouldGo ->
            if (shouldGo) {
                findNavController().navigate(EditPhotoFragmentDirections.actionEditPhotoFragmentToViewPhotoFragment(photoId))
            }
        })

        editPhotoViewModel.goToGalleryList.observe(viewLifecycleOwner, Observer { shouldGo ->
            if (shouldGo) {
                findNavController().navigate(EditPhotoFragmentDirections.actionEditPhotoFragmentToFullGalleryFragment())
            }
        })

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}