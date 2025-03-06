package com.hfad.petlogger.screens.photo.editphoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.R
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.common.usecases.ConfirmActionUseCase
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.databinding.FragmentEditPhotoBinding
import com.hfad.petlogger.databinding.FragmentEditPhotoDetailsBinding
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.events.domain.usecases.GetAllEventsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.events.domain.usecases.GetEventsOfPhotoUseCase
import com.hfad.petlogger.events.domain.usecases.GetMoreOfAllEventsUseCase
import com.hfad.petlogger.events.domain.usecases.GetMoreOfSearchedEventsUseCase
import com.hfad.petlogger.events.domain.usecases.GetSearchedEventsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.notes.domain.usecases.GetAllNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetNotesOfPhotoUseCase
import com.hfad.petlogger.notes.domain.usecases.GetSearchedNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.pets.domain.usecases.GetAllPetsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfAllPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfSearchedPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetPetsOfPhotoUseCase
import com.hfad.petlogger.pets.domain.usecases.GetSearchedPetsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionViewModel
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionViewModel
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.tags.domain.TagRepository
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsUseCase
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsUseCase
import com.hfad.petlogger.tags.domain.usecases.GetTagsOfPhotoUseCase

class EditPhotoFragment : Fragment() {

    private var _binding: FragmentEditPhotoBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null
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
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val photoId = EditPhotoFragmentArgs.fromBundle(requireArguments()).photoId

        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(database, mediaRepository)

        val getAllPetsUseCase = GetMoreOfAllPetsUseCase(petRepository, petsAmt = 10)
        val getPetsOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetPetsOfPhotoUseCase(mediaRepository, photoId))
        val getSearchedPets = GetMoreOfSearchedPetsUseCase(database.petDao, petsAmt = 10)
        val getAllCurrentSelectedPetsFactory = GetAllPetsFromCurrentSelectionUseCaseFactory()
        val getSearchedCurrentSelectedPetsFactory = GetSearchedPetsFromCurrentSelectionUseCaseFactory(database.petDao)
        val petSelectorViewModel = ViewModelProvider(this,
            PetMultiSelectionViewModel.provideFactory(
                getAllPets = getAllPetsUseCase,
                getInitialSelection = getPetsOfPhoto,
                getSearchedSelectionOptions = getSearchedPets,
                getAllCurrentSelectionDisplayFactory = getAllCurrentSelectedPetsFactory,
                getSearchedCurrentSelectionDisplayFactory = getSearchedCurrentSelectedPetsFactory
            )
        ).get(PetMultiSelectionViewModel::class.java)

        val eventRepository = EventRepository(database, mediaRepository)
        val getALlEvents = GetMoreOfAllEventsUseCase(eventRepository, eventAmt = 10)
        val getEventsOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetEventsOfPhotoUseCase(mediaRepository, photoId))
        val getSearchedEvents = GetMoreOfSearchedEventsUseCase(database.eventDao, eventAmt = 10)
        val getAllEventsFromCurrentSelectionFactory = GetAllEventsFromCurrentSelectionUseCaseFactory()
        val getSearchedEventsFromCurrentSelectionFactory = GetSearchedEventsFromCurrentSelectionUseCaseFactory(database.eventDao)
        val eventSelectionViewModel = ViewModelProvider(this,
            EventMultiSelectionViewModel.provideFactory(
                getAllEvents = getALlEvents,
                getAssociatedEvents =  getEventsOfPhoto,
                getSearchedEvents = getSearchedEvents,
                getAllEventsFromCurrentSelection = getAllEventsFromCurrentSelectionFactory,
                getSearchedEventsFromCurrentSelectionFactory = getSearchedEventsFromCurrentSelectionFactory
            )
        ).get(EventMultiSelectionViewModel::class.java)

        val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, noteAmt = 10)
        val getNotesOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetNotesOfPhotoUseCase(mediaRepository, photoId))
        val getSearchedNotesFromAll = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10)
        val getAllNotesFromCurrentSelectionFactory = GetAllNotesFromCurrentSelectionUseCaseFactory()
        val getSearchedNotesFromCurrentSelectionFactory = GetSearchedNotesFromCurrentSelectionUseCaseFactory(database.noteDao)
        val noteSelectionViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getInitialSelection = getNotesOfPhoto,
                getSearchedSelectionOptions = getSearchedNotesFromAll,
                getAllNotesFromCurrentSelectionFactory = getAllNotesFromCurrentSelectionFactory,
                getSearchedNotesFromCurrentSelectionFactory = getSearchedNotesFromCurrentSelectionFactory
            )
        ).get(NoteMultiSelectionViewModel::class.java)

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetTagsOfPhotoUseCase(mediaRepository, photoId))
        val getSearchedTagsFromAll = GetSearchedTagsUseCase(tagRepository)
        val getAllTagsFromCurrentSelectionFactory = GetAllTagsFromCurrentSelectionUseCaseFactory()
        val getSearchedTagsFromCurrentSelectionFactory = GetSearchedTagsFromCurrentSelectionUseCaseFactory(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(
                getAllTags = getAllTags,
                getAllSearchedTagsUseCase = getSearchedTagsFromAll,
                getAllCurrentSelectionFactory = getAllTagsFromCurrentSelectionFactory,
                getSearchedTagsFromCurrentSelectionFactory = getSearchedTagsFromCurrentSelectionFactory,
                getInitialSelection = getTagsOfPhoto
            )
        ).get(TagMultiSelectionViewModel::class.java)

        editPhotoViewModel = ViewModelProvider(this,
            EditPhotoViewModel.provideFactory(mediaRepository, photoId)
        ).get(EditPhotoViewModel::class.java)
        binding.editPhotoViewModel = editPhotoViewModel
        binding.petSelectorViewModel = petSelectorViewModel
        binding.eventSelectorViewModel = eventSelectionViewModel
        binding.noteMultiSelectionViewModel = noteSelectionViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.adapter = EditPhotoViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.pets)
                2 -> getString(R.string.events)
                3 -> getString(R.string.notes)
                else -> null
            }
        }
        mediator?.attach()

        val confirmDelete = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_photo_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_photo_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editPhotoViewModel.deletePhoto() },
            context = requireContext()
        )
        binding.editPhotoTopAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.delete -> {
                    confirmDelete()
                    true
                }
                R.id.submit -> {
                    editPhotoViewModel.submit(
                        petsToAdd = petSelectorViewModel.getPetsToAdd(),
                        petsToRemove = petSelectorViewModel.getPetsToRemove(),
                        eventsToAdd = eventSelectionViewModel.getEventsToAdd(),
                        eventsToRemove = eventSelectionViewModel.getEventsToRemove(),
                        notesToAdd = noteSelectionViewModel.getNotesToAdd(),
                        notesToRemove = noteSelectionViewModel.getNotesToRemove(),
                        tagsToAdd = tagMultiSelectionViewModel.getTagsToAdd(),
                        tagsToRemove = tagMultiSelectionViewModel.getTagsToRemove()
                    )
                    true
                }
                else -> false
            }
        }
        binding.editPhotoTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        editPhotoViewModel.goBack.observe(viewLifecycleOwner, Observer {shouldGo ->
            if (shouldGo) {
                findNavController().navigateSafe(EditPhotoFragmentDirections.actionEditPhotoFragmentToViewPhotoFragment(photoId))
            }
        })

        editPhotoViewModel.goToGalleryList.observe(viewLifecycleOwner, Observer { shouldGo ->
            if (shouldGo) {
                editPhotoViewModel.onNavigateToGalleryList()
                findNavController().navigateSafe(EditPhotoFragmentDirections.actionEditPhotoFragmentToFullGalleryFragment())
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }

    private class EditPhotoViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> EditPhotoDetailsFragment()
                1 -> PetMultiSelectionDisplayFragment()
                2 -> EventMultiSelectionDisplayFragment()
                3 -> NoteMultiSelectionDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class EditPhotoDetailsFragment() : Fragment() {
    private var _binding: FragmentEditPhotoDetailsBinding? = null
    val binding: FragmentEditPhotoDetailsBinding get() = _binding!!
    private val editPhotoViewModel: EditPhotoViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPhotoDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        binding.editPhotoViewModel = editPhotoViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}