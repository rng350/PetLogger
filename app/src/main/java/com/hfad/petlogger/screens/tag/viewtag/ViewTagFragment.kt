package com.hfad.petlogger.screens.tag.viewtag

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentViewTagBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.events.usecases.BuildEventSearchQueryUseCase
import com.hfad.petlogger.events.usecases.GetMoreEventsOfTagUseCase
import com.hfad.petlogger.events.usecases.GetMoreOfSearchedEventsUseCase
import com.hfad.petlogger.notes.usecases.BuildNoteSearchQueryUseCase
import com.hfad.petlogger.notes.usecases.GetMoreNotesOfTagUseCase
import com.hfad.petlogger.notes.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.pets.usecases.BuildPetSearchQueryUseCase
import com.hfad.petlogger.pets.usecases.GetMoreOfSearchedPetsUseCase
import com.hfad.petlogger.pets.usecases.GetMorePetsOfTagUseCase
import com.hfad.petlogger.photos.usecases.BuildPhotoSearchQueryUseCase
import com.hfad.petlogger.photos.usecases.GetMoreOfSearchedPhotosUseCase
import com.hfad.petlogger.photos.usecases.GetMorePhotosOfTagUseCase
import com.hfad.petlogger.screens.event.EventListViewModel
import com.hfad.petlogger.screens.note.NoteListViewModel
import com.hfad.petlogger.screens.pet.PetListViewModel
import com.hfad.petlogger.screens.photo.FullGalleryViewModel
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedEventsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedNotesDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPhotosDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedWeightsForGeneralDisplayFragment
import com.hfad.petlogger.screens.weight.MonitoringListViewModel
import com.hfad.petlogger.weights.usecases.BuildWeightSearchQueryUseCase
import com.hfad.petlogger.weights.usecases.GetMoreWeightsOfTagUseCase
import com.hfad.petlogger.weights.usecases.GetSearchedWeightsForGeneralDisplayUseCase

class ViewTagFragment : Fragment() {
    private var _binding: FragmentViewTagBinding? = null
    val binding: FragmentViewTagBinding get() = _binding!!
    private var mediator: TabLayoutMediator? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewTagBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val tagRepository = TagRepository(database)
        val tagId = ViewTagFragmentArgs.fromBundle(requireArguments()).tagId
        val viewTagViewModel = ViewModelProvider(this,
            ViewTagViewModel.provideFactory(tagRepository, tagId)
        ).get(ViewTagViewModel::class.java)
        binding.viewTagViewModel = viewTagViewModel

        viewTagViewModel.tag.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(
                    title=it.tagName,
                    subtitle = getString(R.string.viewing_tagged_content)
                )
            }
        })

        binding.viewPager.offscreenPageLimit = 5
        binding.viewPager.adapter = ViewTagViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.pets)
                1 -> getString(R.string.weights)
                2 -> getString(R.string.events)
                3 -> getString(R.string.notes)
                4 -> getString(R.string.media)
                else -> null
            }
        }
        mediator?.attach()

        val getPetsOfTag = GetMorePetsOfTagUseCase(database.petDao, tagId, petsAmt=10)
        val getSearchedPetsOfTag = GetMoreOfSearchedPetsUseCase(database.petDao, petsAmt=10, pickFrom = BuildPetSearchQueryUseCase.Pick.FromTag(viewTagViewModel.tag))
        val petListViewModel = ViewModelProvider(this, PetListViewModel.provideFactory(getPetsOfTag, getSearchedPetsOfTag)).get(PetListViewModel::class.java)
        binding.petListViewModel = petListViewModel

        val getWeightsOfTag = GetMoreWeightsOfTagUseCase(database.weightDao, tagId, weightsAmt = 10)
        val getSearchedWeightsOfTag = GetSearchedWeightsForGeneralDisplayUseCase(database.weightDao, weightsAmt = 10, pickFrom = BuildWeightSearchQueryUseCase.Pick.FromTag(viewTagViewModel.tag))
        val weightListViewModel = ViewModelProvider(this, MonitoringListViewModel.provideFactory(getWeightsOfTag, getSearchedWeightsOfTag)).get(MonitoringListViewModel::class.java)
        binding.monitoringListViewModel = weightListViewModel

        val getEventsOfTag = GetMoreEventsOfTagUseCase(database.eventDao, tagId, eventsAmt = 10)
        val getSearchedEventsOfTag = GetMoreOfSearchedEventsUseCase(database.eventDao, eventAmt = 10, pickFrom = BuildEventSearchQueryUseCase.Pick.FromTag(tagId))
        val eventListViewModel = ViewModelProvider(this, EventListViewModel.provideFactory(getEventsOfTag, getSearchedEventsOfTag)).get(EventListViewModel::class.java)
        binding.eventListViewModel = eventListViewModel

        val getNotesOfTag = GetMoreNotesOfTagUseCase(database.noteDao, tagId, notesAmt = 10)
        val getSearchedNotesOfTag = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10, pickFrom = BuildNoteSearchQueryUseCase.Pick.FromTag(tagId))
        val noteListViewModel = ViewModelProvider(this, NoteListViewModel.provideFactory(getNotesOfTag, getSearchedNotesOfTag)).get(NoteListViewModel::class.java)
        binding.noteListViewModel = noteListViewModel

        val getPhotosOfTag = GetMorePhotosOfTagUseCase(database.photoDao, tagId, photosAmt = 10)
        val getSearchedPhotosOfTag = GetMoreOfSearchedPhotosUseCase(database.photoDao, photosAmt = 10, pickFrom = BuildPhotoSearchQueryUseCase.Pick.FromTag(tagId))
        val photoListViewModel = ViewModelProvider(this, FullGalleryViewModel.provideFactory(getPhotosOfTag, getSearchedPhotosOfTag)).get(FullGalleryViewModel::class.java)
        binding.photoListViewModel = photoListViewModel

        petListViewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                petListViewModel.petNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewPetFragment(it))
            }
        })
        weightListViewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                weightListViewModel.weightNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewWeightFragment(it))
            }
        })
        eventListViewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                eventListViewModel.eventNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewEventFragment(it))
            }
        })
        noteListViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                noteListViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewNoteFragment(it))
            }
        })
        photoListViewModel.photoNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                photoListViewModel.photoNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewPhotoFragment(it))
            }
        })

        weightListViewModel.newWeightNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewWeight ->
            if (shouldMakeNewWeight) {
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToNewWeightFragment(tagId=tagId))
                weightListViewModel.newWeightNavigator.onNavigatedToNewEntityScreen()
            }
        }
        eventListViewModel.newEventNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewEvent ->
            if (shouldMakeNewEvent) {
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToNewEventFragment(tagId=tagId))
                eventListViewModel.newEventNavigator.onNavigatedToNewEntityScreen()
            }
        }
        noteListViewModel.newNoteNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewNote ->
            if (shouldMakeNewNote) {
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToNewNoteFragment(tagId=tagId))
                noteListViewModel.newNoteNavigator.onNavigatedToNewEntityScreen()
            }
        }
        photoListViewModel.newPhotoNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewPhoto ->
            if (shouldMakeNewPhoto) {
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToNewPhotoFragment(tagId=tagId))
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
}

class ViewTagViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AssociatedPetsDisplayFragment()
            1 -> AssociatedWeightsForGeneralDisplayFragment()
            2 -> AssociatedEventsDisplayFragment()
            3 -> AssociatedNotesDisplayFragment()
            4 -> AssociatedPhotosDisplayFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}