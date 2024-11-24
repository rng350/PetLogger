package com.hfad.petlogger.screens.note.viewnote

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentViewNoteBinding
import com.hfad.petlogger.databinding.FragmentViewNoteDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.events.usecases.GetMoreEventsOfNoteUseCase
import com.hfad.petlogger.pets.usecases.GetMorePetsOfNoteUseCase
import com.hfad.petlogger.photos.usecases.GetMorePhotosOfNoteUseCase
import com.hfad.petlogger.tags.usecases.GetTagsOfNoteUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedEventsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetsDisplayViewModel
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPhotosDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedTagsDisplayViewModel
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.events.usecases.GetMoreOfSearchedEventsFromAllUseCase
import com.hfad.petlogger.screens.event.EventListViewModel
import com.hfad.petlogger.screens.photo.FullGalleryViewModel

class ViewNoteFragment : Fragment() {
    private var _binding: FragmentViewNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewNoteViewModel: ViewNoteViewModel
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewNoteBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val noteRepository = NoteRepository(database, mediaRepository)
        val noteId = ViewNoteFragmentArgs.fromBundle(requireArguments()).noteId

        viewNoteViewModel = ViewModelProvider(this,
            ViewNoteViewModel.provideFactory(noteRepository, noteId)
        ).get(ViewNoteViewModel::class.java)
        binding.viewNoteViewModel = viewNoteViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val getPhotosOfNote = GetMorePhotosOfNoteUseCase(noteRepository, noteId, photosAmt = 9)
        val photoListViewModel = ViewModelProvider(this, FullGalleryViewModel.provideFactory(getPhotosOfNote)).get(
            FullGalleryViewModel::class.java)
        binding.photoListViewModel = photoListViewModel

        val getAssociatedPets = GetMorePetsOfNoteUseCase(noteRepository, noteId, petsAmt = 10)
        val associatedPetsDisplayViewModel = ViewModelProvider(this, AssociatedPetsDisplayViewModel.provideFactory(getAssociatedPets)).get(
            AssociatedPetsDisplayViewModel::class.java)
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        val getEventsOfNote = GetMoreEventsOfNoteUseCase(noteRepository, noteId, eventAmt = 10)
        val getSearchedEventsTEMPORARY = GetMoreOfSearchedEventsFromAllUseCase(database.eventDao, eventAmt=10)
        val eventListViewModel = ViewModelProvider(this, EventListViewModel.provideFactory(getEventsOfNote, getSearchedEventsTEMPORARY)).get(
            EventListViewModel::class.java)
        binding.eventListViewModel = eventListViewModel

        val tagRepository = TagRepository(database)
        val getTagsOfNote = GetTagsOfNoteUseCase(tagRepository, noteId)
        val associatedTagsViewModel = ViewModelProvider(this, AssociatedTagsDisplayViewModel.provideFactory(getTagsOfNote)).get(
            AssociatedTagsDisplayViewModel::class.java)
        binding.associatedTagsDisplayViewModel = associatedTagsViewModel

        viewNoteViewModel.note.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(title = it.title.ifEmpty { getString(R.string.view_untitled_note_header) }, subtitle = getString(
                    R.string.viewing_note_details
                ))
            }
        })

        binding.editButton.setOnClickListener {
            findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToEditNoteFragment(noteId))
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.adapter = ViewNoteViewPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle
        )
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.pets)
                2 -> getString(R.string.events)
                3 -> getString(R.string.photos_header)
                else -> null
            }
        }
        mediator?.attach()

        // navigate to specific associated entities
        associatedPetsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedPetsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToViewPetFragment(it))
            }
        }
        eventListViewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner) { eventId ->
            eventId?.let {
                eventListViewModel.eventNavigator.onNavigated()
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToViewEventFragment(eventId))
            }
        }
        photoListViewModel.photoNavigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                photoListViewModel.photoNavigator.onNavigated()
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToViewPhotoFragment(it))
            }
        }
        associatedTagsViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedTagsViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToViewTagFragment(it))
            }
        }

        // create new associated entities
        eventListViewModel.newEventNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewEvent ->
            if (shouldMakeNewEvent) {
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToNewEventFragment(noteId=noteId))
                eventListViewModel.newEventNavigator.onNavigatedToNewEntityScreen()
            }
        }
        photoListViewModel.newPhotoNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewPhoto ->
            if (shouldMakeNewPhoto) {
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToNewPhotoFragment(noteId=noteId))
                photoListViewModel.newPhotoNavigator.onNavigatedToNewEntityScreen()
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

    private class ViewNoteViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> ViewNoteDetailsFragment()
                1 -> AssociatedPetsDisplayFragment()
                2 -> AssociatedEventsDisplayFragment()
                3 -> AssociatedPhotosDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class ViewNoteDetailsFragment() : Fragment() {
    private var _binding: FragmentViewNoteDetailsBinding? = null
    val binding: FragmentViewNoteDetailsBinding get() = _binding!!
    private val viewNoteViewModel: ViewNoteViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewNoteDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewNoteViewModel = viewNoteViewModel
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}