package com.hfad.petlogger.screens.pet.viewpet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.hfad.petlogger.databinding.FragmentViewPetBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentViewPetDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.tags.usecases.GetAllTagsOfPetAlphabeticalOrderUseCase
import com.hfad.petlogger.events.usecases.GetMoreEventsOfPetUseCase
import com.hfad.petlogger.notes.usecases.GetMoreNotesOfPetUseCase
import com.hfad.petlogger.photos.usecases.GetMorePhotosOfPetUseCase
import com.hfad.petlogger.weights.usecases.GetMoreWeightsOfPetUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedEventsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedNotesDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetWeightsDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPetWeightsDisplayViewModel
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedPhotosDisplayFragment
import com.hfad.petlogger.screens.sections.associatedentities.AssociatedTagsDisplayViewModel
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.events.usecases.BuildEventSearchQueryUseCase
import com.hfad.petlogger.events.usecases.GetMoreOfSearchedEventsUseCase
import com.hfad.petlogger.notes.usecases.BuildNoteSearchQueryUseCase
import com.hfad.petlogger.notes.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.pets.usecases.GetPetDetailsForDisplayUseCase
import com.hfad.petlogger.photos.usecases.BuildPhotoSearchQueryUseCase
import com.hfad.petlogger.photos.usecases.GetMoreOfSearchedPhotosUseCase
import com.hfad.petlogger.screens.event.EventListViewModel
import com.hfad.petlogger.screens.note.NoteListViewModel
import com.hfad.petlogger.screens.photo.FullGalleryViewModel
import com.hfad.petlogger.weights.usecases.GetSearchedWeightsOfPetForDisplayUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewPetFragment : Fragment() {
    private var _binding: FragmentViewPetBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val petId = ViewPetFragmentArgs.fromBundle(requireArguments()).petId
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getPetDetails = GetPetDetailsForDisplayUseCase(database.petDao, petId)
        val viewPetViewModel = ViewModelProvider(this,
            ViewPetViewModel.provideFactory(getPetDetails)
        ).get(ViewPetViewModel::class.java)
        binding.viewPetViewModel = viewPetViewModel

        val getAssociatedEvents = GetMoreEventsOfPetUseCase(petRepository, petId, eventAmt = 10)
        val getSearchedEvents = GetMoreOfSearchedEventsUseCase(database.eventDao, eventAmt=10, BuildEventSearchQueryUseCase.Pick.FromPet(petId))
        val eventListViewModel = ViewModelProvider(this, EventListViewModel.provideFactory(getAssociatedEvents, getSearchedEvents)).get(
            EventListViewModel::class.java)
        binding.eventListViewModel = eventListViewModel

        val getAssociatedWeights = GetMoreWeightsOfPetUseCase(petRepository, petId, weightsAmt = 10)
        val getSearchedWeights = GetSearchedWeightsOfPetForDisplayUseCase(database.weightDao, weightsAmt = 15, petId)
        val associatedWeightsDisplayViewModel = ViewModelProvider(this, AssociatedPetWeightsDisplayViewModel.provideFactory(getAssociatedWeights, getSearchedWeights)).get(
            AssociatedPetWeightsDisplayViewModel::class.java)
        binding.associatedPetWeightsDisplayViewModel = associatedWeightsDisplayViewModel

        val getPhotosOfPetForDisplayUseCase = GetMorePhotosOfPetUseCase(petRepository, petId, photosAmt = 10)
        val getSearchedPhotosOfPetUseCase = GetMoreOfSearchedPhotosUseCase(
            photoDao = database.photoDao,
            photosAmt = 10,
            pickFrom = BuildPhotoSearchQueryUseCase.Pick.FromPet(petId)
        )
        val photoListViewModel = ViewModelProvider(this, FullGalleryViewModel.provideFactory(getPhotosOfPetForDisplayUseCase, getSearchedPhotosOfPetUseCase)).get(
            FullGalleryViewModel::class.java)
        binding.photoListViewModel = photoListViewModel

        val getNotesOfPet = GetMoreNotesOfPetUseCase(petRepository, petId, notesAmt = 10)
        val getSearchedNotesOfPet = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10, BuildNoteSearchQueryUseCase.Pick.FromPet(petId))
        val noteListViewModel = ViewModelProvider(this, NoteListViewModel.provideFactory(getNotesOfPet, getSearchedNotesOfPet)).get(
            NoteListViewModel::class.java)
        binding.noteListViewModel = noteListViewModel

        val getTagsOfPet = GetAllTagsOfPetAlphabeticalOrderUseCase(petRepository, petId)
        val associatedTagsDisplayViewModel = ViewModelProvider(this, AssociatedTagsDisplayViewModel.provideFactory(getTagsOfPet)).get(
            AssociatedTagsDisplayViewModel::class.java)
        binding.associatedTagsDisplayViewModel = associatedTagsDisplayViewModel

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewPetViewModel.status.collectLatest { status ->
                    when (status) {
                        is ViewPetViewModel.Status.Loaded -> {
                            when (status.result) {
                                GetPetDetailsForDisplayUseCase.Result.Failure -> {
                                    setAppBarTitle(getString(R.string.pet_not_found))
                                }
                                is GetPetDetailsForDisplayUseCase.Result.Success -> {
                                    val fetchedPetName = status.result.fetchedPet.petName
                                    setAppBarTitle(fetchedPetName, getString(R.string.viewing_details))
                                }
                            }
                        }
                        ViewPetViewModel.Status.Loading -> {
                            setAppBarTitle(getString(R.string.loading_pet))
                        }
                    }
                }
            }
        }

        associatedWeightsDisplayViewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner) {weightId ->
            weightId?.let {
                associatedWeightsDisplayViewModel.weightNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewWeightFragment(weightId))
            }
        }
        noteListViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner) { noteId ->
            noteId?.let {
                noteListViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewNoteFragment(noteId))
            }
        }
        eventListViewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner) { eventId ->
            eventId?.let {
                eventListViewModel.eventNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewEventFragment(eventId))
            }
        }
        photoListViewModel.photoNavigator.navigateTo.observe(viewLifecycleOwner) {photoId ->
            photoId?.let {
                photoListViewModel.photoNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewPhotoFragment(photoId))
            }
        }
        associatedTagsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {tagId ->
            tagId?.let {
                associatedTagsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewTagFragment(tagId))
            }
        }

        noteListViewModel.newNoteNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewNote ->
            if (shouldMakeNewNote) {
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToNewNoteFragment(petId=petId))
                noteListViewModel.newNoteNavigator.onNavigatedToNewEntityScreen()
            }
        }
        associatedWeightsDisplayViewModel.newPetWeightNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewWeight ->
            if (shouldMakeNewWeight) {
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToNewWeightFragment(petId=petId))
                associatedWeightsDisplayViewModel.newPetWeightNavigator.onNavigatedToNewEntityScreen()
            }
        }
        eventListViewModel.newEventNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewEvent ->
            if (shouldMakeNewEvent) {
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToNewEventFragment(petId=petId))
                eventListViewModel.newEventNavigator.onNavigatedToNewEntityScreen()
            }
        }
        photoListViewModel.newPhotoNavigator.makeNewEntity.observe(viewLifecycleOwner) { shouldMakeNewPhoto ->
            if (shouldMakeNewPhoto) {
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToNewPhotoFragment(petId=petId))
                photoListViewModel.newPhotoNavigator.onNavigatedToNewEntityScreen()
            }
        }

        binding.editPetButton.setOnClickListener {
            findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToEditPetFragment(petId))
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.viewPager.offscreenPageLimit = 5
        binding.viewPager.adapter = ViewPetViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.profile)
                1 -> getString(R.string.weights)
                2 -> getString(R.string.events)
                3 -> getString(R.string.notes)
                4 -> getString(R.string.media)
                else -> null
            }
        }
        mediator?.attach()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }

    private class ViewPetViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 5
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> PetDetailsFragment()
                1 -> AssociatedPetWeightsDisplayFragment()
                2 -> AssociatedEventsDisplayFragment()
                3 -> AssociatedNotesDisplayFragment()
                4 -> AssociatedPhotosDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class PetDetailsFragment(): Fragment() {
    private var _binding: FragmentViewPetDetailsBinding? = null
    val binding: FragmentViewPetDetailsBinding get() = _binding!!
    private val viewPetViewModel: ViewPetViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPetDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.viewPetViewModel = viewPetViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewPetViewModel.status.collectLatest { status ->
                    when (status) {
                        is ViewPetViewModel.Status.Loaded -> {
                            when (status.result) {
                                GetPetDetailsForDisplayUseCase.Result.Failure -> {
                                    binding.loadingFailedLayout.visibility = View.VISIBLE
                                    binding.petDetailsLayout.visibility = View.GONE
                                    binding.loadingScreenLayout.visibility = View.GONE
                                }
                                is GetPetDetailsForDisplayUseCase.Result.Success -> {
                                    binding.loadingFailedLayout.visibility = View.GONE
                                    binding.petDetailsLayout.visibility = View.VISIBLE
                                    binding.loadingScreenLayout.visibility = View.GONE

                                    status.result.fetchedPet.petProfilePicUri?.let {
                                        Glide.with(requireContext())
                                            .load(it)
                                            .apply(RequestOptions().placeholder(R.drawable.placeholder))
                                            .into(binding.petPhoto)
                                    }

                                    if (status.result.fetchedPet.petDateOfPassing.isNotEmpty()) {
                                        binding.petDateOfPassingTextField.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                        ViewPetViewModel.Status.Loading -> {
                            setAppBarTitle(getString(R.string.loading_pet))
                            binding.loadingFailedLayout.visibility = View.GONE
                            binding.petDetailsLayout.visibility = View.GONE
                            binding.loadingScreenLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}