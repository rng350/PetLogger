package com.hfad.petlogger.screens.event.editevent

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
import com.hfad.petlogger.common.usecases.ConfirmActionUseCase
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.databinding.FragmentEditEventBinding
import com.hfad.petlogger.databinding.FragmentEditEventDetailsBinding
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.notes.domain.usecases.GetAllNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetNotesOfEventUseCase
import com.hfad.petlogger.notes.domain.usecases.GetSearchedNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.pets.domain.usecases.GetAllPetsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfAllPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfSearchedPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetPetsOfEventUseCase
import com.hfad.petlogger.pets.domain.usecases.GetSearchedPetsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.photos.domain.usecases.BuildPhotoSearchQueryUseCase
import com.hfad.petlogger.photos.domain.usecases.GetMoreOfSearchedPhotosUseCase
import com.hfad.petlogger.photos.domain.usecases.GetMorePhotosOfEventUseCase
import com.hfad.petlogger.photos.domain.usecases.GetPhotosOfEventUseCase
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
import com.hfad.petlogger.tags.domain.usecases.GetTagsOfEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditEventFragment : Fragment() {
    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

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
        )
        ).get(EditEventViewModel::class.java)
        binding.viewModel = editEventViewModel

        val petRepository = PetRepository(database, mediaRepository)
        val getAllPetsUseCase = GetMoreOfAllPetsUseCase(petRepository, petsAmt = 10)
        val getPetsOfEventUseCase = GetPetsOfEventUseCase(eventRepository, eventID)
        val getSearchedPets = GetMoreOfSearchedPetsUseCase(database.petDao, petsAmt = 10)
        val getAllCurrentSelectedPetsFactory = GetAllPetsFromCurrentSelectionUseCaseFactory()
        val getSearchedCurrentSelectedPetsFactory = GetSearchedPetsFromCurrentSelectionUseCaseFactory(database.petDao)
        val petMultiSelectionViewModel = ViewModelProvider(this,
            PetMultiSelectionViewModel.provideFactory(
                getAllPets = getAllPetsUseCase,
                getInitialSelection = GetMultipleInitialItemsUseCase.PreExisting(getPetsOfEventUseCase),
                getSearchedSelectionOptions = getSearchedPets,
                getAllCurrentSelectionDisplayFactory = getAllCurrentSelectedPetsFactory,
                getSearchedCurrentSelectionDisplayFactory = getSearchedCurrentSelectedPetsFactory
            )
        ).get(PetMultiSelectionViewModel::class.java)
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel

        val getPhotosOfEvent = GetMultipleInitialItemsUseCase.PreExisting(GetPhotosOfEventUseCase(eventID, eventRepository))
        val getPhotosOfEventPaginated = GetMorePhotosOfEventUseCase(eventRepository, eventID, photosAmt = 10)
        val getSearchedPhotosOfEvent = GetMoreOfSearchedPhotosUseCase(database.photoDao, photosAmt=10, pickFrom = BuildPhotoSearchQueryUseCase.Pick.FromEvent(eventID))
        val mediaSelectionViewModel = ViewModelProvider(this,
            MediaSelectionViewModel.provideFactory(
                mediaRepository = mediaRepository,
                maxItems = 10,
                getInitialSelection = getPhotosOfEvent,
                getAssociatedItems = getPhotosOfEventPaginated,
                getSearchedPhotos = getSearchedPhotosOfEvent
            )
        ).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, noteAmt = 10)
        val getNotesOfEvent = GetNotesOfEventUseCase(eventRepository, eventID)
        val getSearchedNotesFromAll = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10)
        val getAllNotesFromCurrentSelectionFactory = GetAllNotesFromCurrentSelectionUseCaseFactory()
        val getSearchedNotesFromCurrentSelectionFactory = GetSearchedNotesFromCurrentSelectionUseCaseFactory(database.noteDao)
        val noteMultiSelectionViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getInitialSelection = GetMultipleInitialItemsUseCase.PreExisting(getNotesOfEvent),
                getSearchedSelectionOptions = getSearchedNotesFromAll,
                getAllNotesFromCurrentSelectionFactory = getAllNotesFromCurrentSelectionFactory,
                getSearchedNotesFromCurrentSelectionFactory = getSearchedNotesFromCurrentSelectionFactory
            )
        ).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfEvent = GetMultipleInitialItemsUseCase.PreExisting(GetTagsOfEventUseCase(eventRepository, eventID))
        val getSearchedTagsFromAll = GetSearchedTagsUseCase(tagRepository)
        val getAllTagsFromCurrentSelectionFactory = GetAllTagsFromCurrentSelectionUseCaseFactory()
        val getSearchedTagsFromCurrentSelectionFactory = GetSearchedTagsFromCurrentSelectionUseCaseFactory(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(
                getAllTags = getAllTags,
                getAllSearchedTagsUseCase = getSearchedTagsFromAll,
                getAllCurrentSelectionFactory = getAllTagsFromCurrentSelectionFactory,
                getSearchedTagsFromCurrentSelectionFactory = getSearchedTagsFromCurrentSelectionFactory,
                getInitialSelection = getTagsOfEvent
            )
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = EditEventViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                2 -> getString(R.string.photos_header)
                else -> null
            }
        }
        mediator?.attach()

        binding.editEventTopAppBar.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        val confirmDelete = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_event_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_event_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editEventViewModel.deleteEvent()
            },
            context = requireContext()
        )
        binding.editEventTopAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.delete -> {
                    confirmDelete()
                    true
                }
                R.id.submit -> {
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
                    true
                }
                else -> false
            }
        }

        editEventViewModel.goToEventsList.observe(viewLifecycleOwner) {
            if (it == true) {
                editEventViewModel.onNavigateToEventsList()
                this.findNavController().navigateSafe(EditEventFragmentDirections.actionEditEventFragmentToEventListFragment())
            }
        }
        editEventViewModel.goToViewEvent.observe(viewLifecycleOwner) {
            if (it == true) {
                editEventViewModel.onNavigateToViewEvent()
                this.findNavController().navigateSafe(EditEventFragmentDirections.actionEditEventFragmentToViewEventFragment(eventID))
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

    private class EditEventViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> EditEventDetailsFragment()
                1 -> NoteMultiSelectionDisplayFragment()
                2 -> MediaSelectionFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class EditEventDetailsFragment(): Fragment() {
    private var _binding: FragmentEditEventDetailsBinding? = null
    val binding: FragmentEditEventDetailsBinding get() = _binding!!
    private val editEventViewModel: EditEventViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditEventDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = editEventViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

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
                TimePicker.generate(editEventViewModel.eventDateTime, requireContext())
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