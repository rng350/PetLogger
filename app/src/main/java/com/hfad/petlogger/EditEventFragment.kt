package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditEventBinding
import com.hfad.petlogger.entitylinkers.PhotoToEventLinker
import com.hfad.petlogger.photoselection.*
import com.hfad.petlogger.recyclerviews.ItemPickers

class EditEventFragment : Fragment() {
    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    /*private var _galleryPicker: GalleryPicker? = null
    private val galleryPicker get() = _galleryPicker!!*/
    private var _galleryEditDisplay: GalleryEditDisplay? = null
    private val galleryEditDisplay get() = _galleryEditDisplay

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

        val galleryEditDisplayViewModelFactory = GalleryEditDisplayViewModelFactory(
            associatedID = editEventViewModel.eventID,
            eventDao = eventDao,
            choiceLimit = 10
        )
        val galleryEditDisplayViewModel = ViewModelProvider(this, galleryEditDisplayViewModelFactory).get((GalleryEditDisplayViewModel::class.java))
        binding.galleryEditDisplayViewModel = galleryEditDisplayViewModel

        editEventViewModel.event.observe(viewLifecycleOwner, Observer {
            it?.let {
                val mainActivity = (activity as MainActivity)
                mainActivity.setTopAppBarTitle(it.title)
                mainActivity.setTopAppBarSubtitle(getString(R.string.editing_event_details))
            }
        })

        val galleryViewModelFactory = GalleryViewModelFactory(
            entityLinker = PhotoToEventLinker(photoDao),
            photoDao = photoDao,
            photosSelected = galleryEditDisplayViewModel.newPhotosAssociatedTracker)
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)
        binding.galleryViewModel = galleryViewModel

        _galleryEditDisplay = GalleryEditDisplay(
            binding.galleryEditPicker,
            photoDao,
            eventDao,
            this,
            editEventViewModel.eventID,
            galleryEditDisplayViewModel,
            galleryViewModel
        )
        galleryEditDisplay?.onCreate(savedInstanceState)

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
            editEventViewModel.initRecyclerViewPetList()
            Log.e("petlist", "recyc initialized")
        })

        editEventViewModel.allPets.observeOnce(viewLifecycleOwner, Observer {
            editEventViewModel.allPetsFetched = true
            editEventViewModel.initRecyclerViewPetList()
        })

        editEventViewModel.initialPhotoSelection.observeOnce(viewLifecycleOwner, Observer {
            editEventViewModel.initAssociatedPhotoList()
        })

        binding.inputEventDateButton.setOnClickListener {
            // TODO: Implement
        }

        binding.inputEventTimeButton.setOnClickListener{
            // TODO: Implement
        }

        binding.submitChangesButton.setOnClickListener {
            // TODO: submit stuff
            this.findNavController().navigate(EditEventFragmentDirections.actionEditEventFragmentToViewEventFragment(eventID))
        }

        binding.cancelButton.setOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.deleteEventButton.setOnClickListener {
            // TODO: delete
            this.findNavController().navigate(R.id.action_editEventFragment_to_homeFragment)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        galleryEditDisplay?.onResume()
        /*galleryPicker.onResume()*/
    }

    override fun onDestroy() {
        super.onDestroy()
        galleryEditDisplay?.onDestroy()
        /*galleryPicker.onDestroy()*/
        _binding = null
    }
}