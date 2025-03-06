package com.hfad.petlogger.screens.event.newevent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.R
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.common.datetimeselection.DatePicker
import com.hfad.petlogger.common.datetimeselection.TimePicker
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.util.Constants.Companion.defaultNullIdForNavigation
import com.hfad.petlogger.databinding.FragmentNewEventBinding
import com.hfad.petlogger.databinding.FragmentNewEventDetailsBinding
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.notes.domain.usecases.GetAllNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetSearchedNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.usecases.GetSingleNoteUseCase
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.pets.domain.usecases.GetAllPetsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfAllPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfSearchedPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetSearchedPetsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.usecases.GetSinglePetUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.photos.domain.usecases.GetSinglePhotoUseCase
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionViewModel
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionFragment
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionViewModel
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.tags.domain.TagRepository
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsUseCase
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewEventFragment : Fragment() {
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

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
        val newEventViewModel = ViewModelProvider(this,
            NewEventViewModel.provideFactory(eventRepository)
        ).get(NewEventViewModel::class.java)
        binding.newEventViewModel = newEventViewModel

        val photoId = NewEventFragmentArgs.fromBundle(requireArguments()).photoId
        val getAssociatedPhoto = if (photoId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSinglePhotoUseCase(database.photoDao, photoId))
        } else null
        val mediaSelectionViewModel = ViewModelProvider(this,
            MediaSelectionViewModel.provideFactory(
                mediaRepository = mediaRepository,
                maxItems = 10,
                getInitialSelection = getAssociatedPhoto,
            )
        ).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val petRepository = PetRepository(database, mediaRepository)
        val getAllPetsUseCase = GetMoreOfAllPetsUseCase(petRepository, petsAmt = 10)
        val petId = NewEventFragmentArgs.fromBundle(requireArguments()).petId
        val getAssociatedPet = if (petId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSinglePetUseCase(database.petDao, petId))
        } else null
        val getSearchedPets = GetMoreOfSearchedPetsUseCase(database.petDao, petsAmt = 10)
        val getAllCurrentSelectedPetsFactory = GetAllPetsFromCurrentSelectionUseCaseFactory()
        val getSearchedCurrentSelectedPetsFactory = GetSearchedPetsFromCurrentSelectionUseCaseFactory(database.petDao)
        val petMultiSelectionViewModel = ViewModelProvider(this,
            PetMultiSelectionViewModel.provideFactory(
                getAllPets = getAllPetsUseCase,
                getInitialSelection = getAssociatedPet,
                getSearchedSelectionOptions = getSearchedPets,
                getAllCurrentSelectionDisplayFactory = getAllCurrentSelectedPetsFactory,
                getSearchedCurrentSelectionDisplayFactory = getSearchedCurrentSelectedPetsFactory
            )
        ).get(PetMultiSelectionViewModel::class.java)
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, noteAmt = 10)
        val noteId = NewEventFragmentArgs.fromBundle(requireArguments()).noteId
        val getAssociatedNote = if (noteId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSingleNoteUseCase(database.noteDao, noteId))
        } else null
        val getSearchedNotesFromAll = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10)
        val getAllNotesFromCurrentSelectionFactory = GetAllNotesFromCurrentSelectionUseCaseFactory()
        val getSearchedNotesFromCurrentSelectionFactory = GetSearchedNotesFromCurrentSelectionUseCaseFactory(database.noteDao)
        val noteMultiSelectionViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getInitialSelection = getAssociatedNote,
                getSearchedSelectionOptions = getSearchedNotesFromAll,
                getAllNotesFromCurrentSelectionFactory = getAllNotesFromCurrentSelectionFactory,
                getSearchedNotesFromCurrentSelectionFactory = getSearchedNotesFromCurrentSelectionFactory
            )
        ).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getSearchedTagsFromAll = GetSearchedTagsUseCase(tagRepository)
        val getAllTagsFromCurrentSelectionFactory = GetAllTagsFromCurrentSelectionUseCaseFactory()
        val getSearchedTagsFromCurrentSelectionFactory = GetSearchedTagsFromCurrentSelectionUseCaseFactory(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(
                getAllTags = getAllTags,
                getAllSearchedTagsUseCase = getSearchedTagsFromAll,
                getAllCurrentSelectionFactory = getAllTagsFromCurrentSelectionFactory,
                getSearchedTagsFromCurrentSelectionFactory = getSearchedTagsFromCurrentSelectionFactory
            )
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = NewEventViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                2 -> getString(R.string.photos_header)
                else -> null
            }
        }
        mediator?.attach()

        binding.newEventTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.newEventTopAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.submit -> {
                    newEventViewModel.submitEvent(
                        pets = petMultiSelectionViewModel.getPetsToAdd(),
                        photos = mediaSelectionViewModel.getPhotosToAdd(),
                        notes = noteMultiSelectionViewModel.getNotesToAdd(),
                        tags = tagMultiSelectionViewModel.getTagsToAdd()
                    )
                    true
                }
                else -> false
            }
        }

        newEventViewModel.carryOn.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigateSafe(NewEventFragmentDirections.actionNewEventFragmentToEventListFragment())
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }

    private class NewEventViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> NewEventDetailsFragment()
                1 -> NoteMultiSelectionDisplayFragment()
                2 -> MediaSelectionFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class NewEventDetailsFragment() : Fragment() {
    private var _binding: FragmentNewEventDetailsBinding? = null
    val binding: FragmentNewEventDetailsBinding get() = _binding!!
    private val newEventViewModel: NewEventViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewEventDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        binding.newEventViewModel = newEventViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

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