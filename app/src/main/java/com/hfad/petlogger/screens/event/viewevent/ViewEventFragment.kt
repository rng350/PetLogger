package com.hfad.petlogger.screens.event.viewevent

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
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.databinding.FragmentViewEventBinding
import com.hfad.petlogger.databinding.FragmentViewEventDetailsBinding
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.notes.domain.usecases.BuildNoteSearchQueryUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreNotesOfEventUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.pets.domain.usecases.GetMorePetsOfEventUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.photos.domain.usecases.BuildPhotoSearchQueryUseCase
import com.hfad.petlogger.photos.domain.usecases.GetMoreOfSearchedPhotosUseCase
import com.hfad.petlogger.photos.domain.usecases.GetMorePhotosOfEventUseCase
import com.hfad.petlogger.screens.note.NoteListViewModel
import com.hfad.petlogger.screens.photo.FullGalleryViewModel
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedNotesDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetsDisplayViewModel
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPhotosDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedTagsDisplayViewModel
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsOfEventAlphabeticalOrderUseCase

class ViewEventFragment : Fragment() {
    private var _binding: FragmentViewEventBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewEventBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val eventDao = database.eventDao
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val eventRepository = EventRepository(database, mediaRepository)

        val eventId = ViewEventFragmentArgs.fromBundle(requireArguments()).eventId
        val viewEventViewModel = ViewModelProvider(this,
            ViewEventViewModel.provideFactory(eventDao, eventId)
        ).get(ViewEventViewModel::class.java)
        binding.viewEventViewModel = viewEventViewModel

        val getAssociatedPhotos = GetMorePhotosOfEventUseCase(eventRepository, eventId, photosAmt = 10)
        val getSearchedPhotosOfEvent = GetMoreOfSearchedPhotosUseCase(
            photoDao = database.photoDao,
            photosAmt = 10,
            pickFrom = BuildPhotoSearchQueryUseCase.Pick.FromEvent(eventId)
        )
        val galleryViewModel = ViewModelProvider(this, FullGalleryViewModel.provideFactory(getAssociatedPhotos, getSearchedPhotosOfEvent)).get(
            FullGalleryViewModel::class.java)
        binding.photoListViewModel = galleryViewModel

        val getAssociatedPets = GetMorePetsOfEventUseCase(eventRepository, eventId, petsAmt = 10)
        val associatedPetsDisplayViewModel = ViewModelProvider(this, AssociatedPetsDisplayViewModel.provideFactory(getAssociatedPets)).get(
            AssociatedPetsDisplayViewModel::class.java)
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        val getNotesOfEvent = GetMoreNotesOfEventUseCase(eventRepository, eventId, amtLimit = 10)
        val getSearchedNotesOfEvent = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10, BuildNoteSearchQueryUseCase.Pick.FromEvent(eventId))
        val noteListViewModel = ViewModelProvider(this, NoteListViewModel.provideFactory(getNotesOfEvent, getSearchedNotesOfEvent)).get(
            NoteListViewModel::class.java)
        binding.noteListViewModel = noteListViewModel

        val getTagsOfEvent = GetAllTagsOfEventAlphabeticalOrderUseCase(eventRepository, eventId)
        val associatedTagsDisplayViewModel = ViewModelProvider(this, AssociatedTagsDisplayViewModel.provideFactory(getTagsOfEvent)).get(
            AssociatedTagsDisplayViewModel::class.java)
        binding.associatedTagsDisplayViewModel = associatedTagsDisplayViewModel

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = ViewEventViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                2 -> getString(R.string.photos_header)
                else -> null
            }
        }
        mediator?.attach()

        associatedPetsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedPetsDisplayViewModel.navigator.onNavigated()
                this.findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToViewPetFragment(it))
            }
        }
        galleryViewModel.photoNavigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                galleryViewModel.photoNavigator.onNavigated()
                findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToViewPhotoFragment(it))
            }
        }
        noteListViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                noteListViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToViewNoteFragment(it))
            }
        }
        associatedTagsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) { tagId ->
            tagId?.let {
                associatedTagsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToViewTagFragment(tagId))
            }
        }

        noteListViewModel.newNoteNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewNote ->
            if (shouldMakeNewNote) {
                findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToNewNoteFragment(eventId=eventId))
                noteListViewModel.newNoteNavigator.onNavigatedToNewEntityScreen()
            }
        }
        galleryViewModel.newPhotoNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewPhoto ->
            if (shouldMakeNewPhoto) {
                findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToNewPhotoFragment(eventId=eventId))
                galleryViewModel.newPhotoNavigator.onNavigatedToNewEntityScreen()
            }
        }

        binding.viewEventTopAppBar.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }
        binding.viewEventTopAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.edit -> {
                    findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToEditEventFragment(eventId))
                    true
                }
                else -> false
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

    private class ViewEventViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> ViewEventDetailsFragment()
                1 -> AssociatedNotesDisplayFragment()
                2 -> AssociatedPhotosDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class ViewEventDetailsFragment() : Fragment() {
    private var _binding: FragmentViewEventDetailsBinding? = null
    val binding: FragmentViewEventDetailsBinding get() = _binding!!
    private val viewEventViewModel: ViewEventViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewEventDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewEventViewModel = viewEventViewModel

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}