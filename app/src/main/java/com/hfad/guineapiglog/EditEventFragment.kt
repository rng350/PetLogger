package com.hfad.guineapiglog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.guineapiglog.databinding.FragmentEditEventBinding

class EditEventFragment : Fragment() {
    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    private var _galleryPicker: GalleryPicker? = null
    private val galleryPicker get() = _galleryPicker!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application

        val eventID = EditEventFragmentArgs.fromBundle(requireArguments()).eventId

        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val photoDao = PetLoggerDatabase.getInstance(application).photoDao
        val eventDao = PetLoggerDatabase.getInstance(application).eventDao

        val editEventViewModelFactory = EditEventViewModelFactory(eventID, eventDao, petDao)
        val editEventViewModel = ViewModelProvider(this, editEventViewModelFactory).get(EditEventViewModel::class.java)
        binding.viewModel = editEventViewModel

        val galleryViewModelFactory = GalleryViewModelFactory(entityLinker = PhotoToEventLinker(photoDao), choiceLimit = 10, photoDao = photoDao)
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)
        binding.galleryViewModel = galleryViewModel

        _galleryPicker = GalleryPicker(this, binding.galleryPicker, galleryViewModel, associatedID = editEventViewModel.eventID)
        galleryPicker.onCreate(savedInstanceState)

        ItemPickers.setupPetWithProfilePhotoEditPicker(
            editEventViewModel.pets,
            editEventViewModel.petsAssociated,
            binding.petSelector,
            viewLifecycleOwner,
            requireContext())

        editEventViewModel.event.observe(viewLifecycleOwner, Observer {
            editEventViewModel.onEventFetched()
        })

        editEventViewModel.initialPetSelection.observeOnce(viewLifecycleOwner, Observer {
            Log.e("petlist", "about to initialize shit")
            editEventViewModel.petsAssociated.initializeSelection(it)
            editEventViewModel.associatedPetsFetched = true
            Log.e("petlist", "selectopm initialized: ${editEventViewModel.petsAssociated.initialSelection}")
            editEventViewModel.initRecyclerViewPetList()
            Log.e("petlist", "recyc initialized")
        })

        editEventViewModel.allPets.observeOnce(viewLifecycleOwner, Observer {
            editEventViewModel.allPetsFetched = true
            editEventViewModel.initRecyclerViewPetList()
        })

        editEventViewModel.initialPhotoSelection.observeOnce(viewLifecycleOwner, Observer {
            editEventViewModel.photosAssociated.initializeSelection(it)
            editEventViewModel.initRecyclerViewPhotosList()
        })

        binding.submitChangesButton.setOnClickListener {
            // TODO: submit stuff
            this.findNavController().navigate(EditEventFragmentDirections.actionEditEventFragmentToViewEventFragment(eventID))
        }

        binding.cancelButton.setOnClickListener {
            this.findNavController().navigate(EditEventFragmentDirections.actionEditEventFragmentToViewEventFragment(eventID))
        }

        binding.deleteEventButton.setOnClickListener {
            // TODO: delete
            this.findNavController().navigate(R.id.action_editEventFragment_to_homeFragment)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        galleryPicker.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        galleryPicker.onDestroy()
        _binding = null
    }
}