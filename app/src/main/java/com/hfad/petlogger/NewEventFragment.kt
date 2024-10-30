package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNewEventBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllNotesUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetNotesOfEventUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.TagRepository
import kotlinx.coroutines.*

class NewEventFragment : Fragment() {
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewEventBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val eventRepository = EventRepository(database, mediaRepository)
        val newEventViewModel = ViewModelProvider(this, NewEventViewModel.provideFactory(eventRepository)).get(NewEventViewModel::class.java)
        binding.newEventViewModel = newEventViewModel

        val mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(mediaRepository=mediaRepository, maxItems=10)).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val petRepository = PetRepository(database, mediaRepository)
        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val petMultiSelectionViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(getAllPetsUseCase)).get(PetMultiSelectionViewModel::class.java)
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotesUseCase = GetAllNotesUseCase(noteRepository)
        val noteMultiSelectionViewModel = ViewModelProvider(this, NoteMultiSelectionViewModel.provideFactory(getAllNotesUseCase)).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this, TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags)).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        setAppBarTitle(getString(R.string.new_event_header))

        binding.eventDate.setOnClickListener {
            binding.eventDate.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                DatePicker.generate(newEventViewModel.eventDateTime)
                    .show(parentFragmentManager, "DATE_PICKER")
                delay(200)
                binding.eventDate.isEnabled = true
            }
        }

        binding.eventTime.setOnClickListener {
            binding.eventTime.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                TimePicker.generate(newEventViewModel.eventDateTime, requireContext())
                    .show(parentFragmentManager, "TIME_PICKER")
                delay(200)
                binding.eventTime.isEnabled = true
            }
        }

        binding.submitEventButton.setOnClickListener {
            newEventViewModel.submitEvent(
                pets = petMultiSelectionViewModel.getPetsToAdd(),
                photos = mediaSelectionViewModel.getPhotosToAdd(),
                notes = noteMultiSelectionViewModel.getNotesToAdd(),
                tags = tagMultiSelectionViewModel.getTagsToAdd()
            )
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        newEventViewModel.carryOn.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigateSafe(NewEventFragmentDirections.actionNewEventFragmentToEventListFragment())
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