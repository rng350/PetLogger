package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditEventBinding
import com.hfad.petlogger.entitylinkers.PhotoToEventLinker
import com.hfad.petlogger.photodisplay.stateless.GetPhotosOfEventUseCase
import com.hfad.petlogger.photoselection.*
import com.hfad.petlogger.recyclerviews.ItemPickers
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository

class EditEventFragment : Fragment() {
    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val eventID = EditEventFragmentArgs.fromBundle(requireArguments()).eventId
        val mediaRepository = MediaRepository(database, requireContext())
        val eventRepository = EventRepository(database, mediaRepository)

        val petDao = database.petDao
        val eventDao = database.eventDao

        val editEventViewModel = ViewModelProvider(this, EditEventViewModel.provideFactory(eventID, eventDao, petDao)).get(EditEventViewModel::class.java)
        binding.viewModel = editEventViewModel

        val getPhotosOfEventUseCase = GetPhotosOfEventUseCase(eventID, eventRepository)
        val mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(
            mediaRepository = mediaRepository,
            fetchInitialSelection = getPhotosOfEventUseCase,
            maxItems = 10)).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        editEventViewModel.event.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(title = it.title, subtitle = getString(R.string.editing_event_details))
            }
        })

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

        binding.inputEventDateButton.setOnClickListener {
            DatePicker.generate(editEventViewModel.eventDateTime).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.inputEventTimeButton.setOnClickListener{
            TimePicker.generate(editEventViewModel.eventDateTime, requireContext()).show(parentFragmentManager, "TIME_PICKER")
        }

        binding.submitChangesButton.setOnClickListener {
            editEventViewModel.submitChanges()
            this.findNavController().navigate(EditEventFragmentDirections.actionEditEventFragmentToViewEventFragment(eventID))
        }

        binding.cancelButton.setOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.deleteEventButton.setOnClickListener {
            editEventViewModel.deleteEvent()
            this.findNavController().navigate(R.id.action_editEventFragment_to_homeFragment)
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}