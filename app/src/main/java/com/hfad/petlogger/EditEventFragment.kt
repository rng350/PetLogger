package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditEventBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllNotesUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetNotesOfEventUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPetsOfEventUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPhotosOfEventUseCase
import com.hfad.petlogger.photodisplay.stateless.GetTagsOfEventUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val eventRepository = EventRepository(database, mediaRepository)

        val editEventViewModel = ViewModelProvider(this, EditEventViewModel.provideFactory(
            eventRepository,
            eventID
        )).get(EditEventViewModel::class.java)
        binding.viewModel = editEventViewModel

        val petRepository = PetRepository(database, mediaRepository)
        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val getPetsOfEventUseCase = GetPetsOfEventUseCase(eventRepository, eventID)
        val petMultiSelectionViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(getAllPets = getAllPetsUseCase, getInitialSelection = getPetsOfEventUseCase)).get(PetMultiSelectionViewModel::class.java)
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel

        val getPhotosOfEventUseCase = GetPhotosOfEventUseCase(eventID, eventRepository)
        val mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(
            mediaRepository = mediaRepository,
            fetchInitialSelection = getPhotosOfEventUseCase,
            maxItems = 10)).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetAllNotesUseCase(noteRepository)
        val getNotesOfEvent = GetNotesOfEventUseCase(eventRepository, eventID)
        val noteMultiSelectionViewModel = ViewModelProvider(this, NoteMultiSelectionViewModel.provideFactory(getAllNotes = getAllNotes, getInitialSelection = getNotesOfEvent)).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfEvent = GetTagsOfEventUseCase(eventRepository, eventID)
        val tagMultiSelectionViewModel = ViewModelProvider(this, TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags, getTagsOfEvent)).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        editEventViewModel.event.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(title = it.title, subtitle = getString(R.string.editing_event_details))
            }
        })

        binding.eventDate.setOnClickListener {
            binding.eventDate.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                DatePicker.generate(editEventViewModel.eventDateTime).show(parentFragmentManager, "DATE_PICKER")
                delay(200)
                binding.eventDate.isEnabled = true
            }
        }

        binding.eventTime.setOnClickListener{
            binding.eventTime.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                TimePicker.generate(editEventViewModel.eventDateTime, requireContext()).show(parentFragmentManager, "TIME_PICKER")
                delay(200)
                binding.eventTime.isEnabled = true
            }
        }

        binding.submitChangesButton.setOnClickListener {
            editEventViewModel.submitChanges(
                petsToAdd = petMultiSelectionViewModel.getPetsToAdd(),
                petsToRemove = petMultiSelectionViewModel.getPetsToRemove(),
                photosToAdd = mediaSelectionViewModel.getPhotosToAdd(),
                photosToRemove = mediaSelectionViewModel.getPhotosToRemove(),
                notesToAdd = noteMultiSelectionViewModel.getNotesToAdd(),
                notesToRemove = noteMultiSelectionViewModel.getNotesToRemove(),
                tagsToAdd = tagMultiSelectionViewModel.getTagsToAdd(),
                tagsToRemove = tagMultiSelectionViewModel.getTagsToRemove()
            )
            this.findNavController().navigateSafe(EditEventFragmentDirections.actionEditEventFragmentToViewEventFragment(eventID))
        }

        binding.cancelButton.setOnClickListener {
            this.findNavController().popBackStack()
        }

        val confirmAction = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_event_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_event_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editEventViewModel.deleteEvent()
            },
            context = requireContext()
        )
        binding.deleteEventButton.setOnClickListener {
            confirmAction()
        }
        editEventViewModel.goToEventsList.observe(viewLifecycleOwner) {
            if (it == true) {
                editEventViewModel.onNavigateToEventsList()
                this.findNavController().navigateSafe(EditEventFragmentDirections.actionEditEventFragmentToEventListFragment())
            }
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        binding.eventDate.isEnabled = true
        binding.eventTime.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}