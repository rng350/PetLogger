package com.hfad.petlogger.screens.photo.viewphoto

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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentViewPhotoBinding
import com.hfad.petlogger.databinding.FragmentViewPhotoDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.tags.usecases.GetAllTagsOfPhotoAlphabeticalOrderUseCase
import com.hfad.petlogger.events.usecases.GetMoreEventsOfPhotoUseCase
import com.hfad.petlogger.notes.usecases.GetMoreNotesOfPhotoUseCase
import com.hfad.petlogger.pets.usecases.GetMorePetsOfPhotoUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedEventsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedEventsDisplayViewModel
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedNotesDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetsDisplayViewModel
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedTagsDisplayViewModel
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.events.usecases.GetMoreOfSearchedEventsFromAllUseCase
import com.hfad.petlogger.notes.usecases.GetMoreOfSearchedNotesOfPhotoUseCase
import com.hfad.petlogger.screens.event.EventListViewModel
import com.hfad.petlogger.screens.note.NoteListViewModel

class ViewPhotoFragment : Fragment() {
    private var _binding: FragmentViewPhotoBinding? = null
    val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPhotoBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val photoId = ViewPhotoFragmentArgs.fromBundle(requireArguments()).photoId
        val viewPhotoViewModel = ViewModelProvider(this,
            ViewPhotoViewModel.provideFactory(mediaRepository, photoId)
        ).get(ViewPhotoViewModel::class.java)
        binding.viewPhotoViewModel = viewPhotoViewModel

        setAppBarTitle(getString(R.string.viewing_photo_details))

        val getPetsOfPhotoForDisplayUseCase = GetMorePetsOfPhotoUseCase(mediaRepository, photoId, petsAmt = 10)
        val associatedPetsDisplayViewModel = ViewModelProvider(this, AssociatedPetsDisplayViewModel.provideFactory(getPetsOfPhotoForDisplayUseCase)).get(
            AssociatedPetsDisplayViewModel::class.java)
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        val getEventsOfPhotoForDisplayUseCase = GetMoreEventsOfPhotoUseCase(mediaRepository, photoId, eventAmt = 10)
        val getSearchedEventsTEMPORARY = GetMoreOfSearchedEventsFromAllUseCase(database.eventDao, eventAmt=10)
        val eventListViewModel = ViewModelProvider(this, EventListViewModel.provideFactory(getEventsOfPhotoForDisplayUseCase, getSearchedEventsTEMPORARY)).get(
            EventListViewModel::class.java)
        binding.eventListViewModel = eventListViewModel

        val getNotesOfPhoto = GetMoreNotesOfPhotoUseCase(mediaRepository, photoId, notesAmt = 10)
        val getSearchedNotesOfPhoto = GetMoreOfSearchedNotesOfPhotoUseCase(mediaRepository, photoId, notesAmt = 10)
        val noteListViewModel = ViewModelProvider(this, NoteListViewModel.provideFactory(getNotesOfPhoto, getSearchedNotesOfPhoto)).get(
            NoteListViewModel::class.java)
        binding.noteListViewModel = noteListViewModel

        val getTagsOfPhotoAlphabeticalOrder = GetAllTagsOfPhotoAlphabeticalOrderUseCase(mediaRepository, photoId)
        val associatedTagsDisplayViewModel = ViewModelProvider(this, AssociatedTagsDisplayViewModel.provideFactory(getTagsOfPhotoAlphabeticalOrder)).get(
            AssociatedTagsDisplayViewModel::class.java)
        binding.associatedTagsDisplayViewModel = associatedTagsDisplayViewModel

        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.adapter = ViewPhotoViewPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle
        )
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

        associatedPetsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner, Observer { petId ->
            petId?.let {
                associatedPetsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToViewPetFragment(petId))
            }
        })
        eventListViewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer { eventId ->
            eventId?.let {
                eventListViewModel.eventNavigator.onNavigated()
                findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToViewEventFragment(eventId))
            }
        })
        noteListViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner, Observer { noteId ->
            noteId?.let {
                noteListViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToViewNoteFragment(noteId))
            }
        })
        associatedTagsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner, Observer { tagId ->
            tagId?.let {
                associatedTagsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToViewTagFragment(tagId))
            }
        })

        noteListViewModel.newNoteNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewNote ->
            if (shouldMakeNewNote) {
                findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToNewNoteFragment(photoId=photoId))
                noteListViewModel.newNoteNavigator.onNavigatedToNewEntityScreen()
            }
        }
        eventListViewModel.newEventNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewEvent ->
            if (shouldMakeNewEvent) {
                findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToNewEventFragment(photoId=photoId))
                eventListViewModel.newEventNavigator.onNavigatedToNewEntityScreen()
            }
        }

        binding.editButton.setOnClickListener{
            findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToEditPhotoFragment(photoId))
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
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

    private class ViewPhotoViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> ViewPhotoDetailsFragment()
                1 -> AssociatedPetsDisplayFragment()
                2 -> AssociatedEventsDisplayFragment()
                3 -> AssociatedNotesDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class ViewPhotoDetailsFragment() : Fragment() {
    private var _binding: FragmentViewPhotoDetailsBinding? = null
    val binding: FragmentViewPhotoDetailsBinding get() = _binding!!
    private val viewPhotoViewModel: ViewPhotoViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPhotoDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewPhotoViewModel = viewPhotoViewModel

        viewPhotoViewModel.photo.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.photoDisplay)
            } else binding.photoDisplay.setImageResource(R.drawable.placeholder)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}