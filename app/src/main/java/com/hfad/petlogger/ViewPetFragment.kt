package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.hfad.petlogger.databinding.FragmentViewPetBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.databinding.FragmentViewPetDetailsBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsOfPetAlphabeticalOrderUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreEventsOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreNotesOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMorePhotosOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreWeightsOfPetUseCase
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.util.GetPeriodDisplayUseCase
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
        val getPetAgeDisplay = GetPeriodDisplayUseCase()
        val viewPetViewModel = ViewModelProvider(this, ViewPetViewModel.provideFactory(petRepository, petId, getPetAgeDisplay)).get(ViewPetViewModel::class.java)
        binding.viewPetViewModel = viewPetViewModel

        val getAssociatedEvents = GetMoreEventsOfPetUseCase(petRepository, petId, eventAmt = 10)
        val associatedEventsDisplayViewModel = ViewModelProvider(this, AssociatedEventsDisplayViewModel.provideFactory(getAssociatedEvents)).get(AssociatedEventsDisplayViewModel::class.java)
        binding.associatedEventsDisplayViewModel = associatedEventsDisplayViewModel

        val getAssociatedWeights = GetMoreWeightsOfPetUseCase(petRepository, petId, weightsAmt = 10)
        val associatedWeightsDisplayViewModel = ViewModelProvider(this, AssociatedPetWeightsDisplayViewModel.provideFactory(getAssociatedWeights)).get(AssociatedPetWeightsDisplayViewModel::class.java)
        binding.associatedPetWeightsDisplayViewModel = associatedWeightsDisplayViewModel

        val getPhotosOfPetForDisplayUseCase = GetMorePhotosOfPetUseCase(petRepository, petId, photosAmt = 10)
        val associatedPhotosDisplayViewModel = ViewModelProvider(this, AssociatedPhotosDisplayViewModel.provideFactory(getPhotosOfPetForDisplayUseCase)).get(AssociatedPhotosDisplayViewModel::class.java)
        binding.associatedPhotosDisplayViewModel = associatedPhotosDisplayViewModel

        val getNotesOfPet = GetMoreNotesOfPetUseCase(petRepository, petId, notesAmt = 10)
        val associatedNotesDisplayViewModel = ViewModelProvider(this, AssociatedNotesDisplayViewModel.provideFactory(getNotesOfPet)).get(AssociatedNotesDisplayViewModel::class.java)
        binding.associatedNotesDisplayViewModel = associatedNotesDisplayViewModel

        val getTagsOfPet = GetAllTagsOfPetAlphabeticalOrderUseCase(petRepository, petId)
        val associatedTagsDisplayViewModel = ViewModelProvider(this, AssociatedTagsDisplayViewModel.provideFactory(getTagsOfPet)).get(AssociatedTagsDisplayViewModel::class.java)
        binding.associatedTagsDisplayViewModel = associatedTagsDisplayViewModel

        viewPetViewModel.pet.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(it.petName, getString(R.string.viewing_details))
            }
        })


        associatedWeightsDisplayViewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner) {weightId ->
            weightId?.let {
                associatedWeightsDisplayViewModel.weightNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewWeightFragment(weightId))
            }
        }
        associatedNotesDisplayViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner) { noteId ->
            noteId?.let {
                associatedNotesDisplayViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewNoteFragment(noteId))
            }
        }
        associatedEventsDisplayViewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner) { eventId ->
            eventId?.let {
                associatedEventsDisplayViewModel.eventNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewEventFragment(eventId))
            }
        }
        associatedPhotosDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {photoId ->
            photoId?.let {
                associatedPhotosDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewPhotoFragment(photoId))
            }
        }
        associatedTagsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {tagId ->
            tagId?.let {
                associatedTagsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewTagFragment(tagId))
            }
        }

        binding.editPetButton.setOnClickListener {
            findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToEditPetFragment(petId))
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

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

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                associatedWeightsDisplayViewModel.weights.collectLatest{petWeights ->
                    viewPetViewModel.setLatestWeight(petWeights)
                }
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

        viewPetViewModel.petProfilePhoto.observe(viewLifecycleOwner, Observer { it ->
            Glide.with(requireContext())
                .load(it.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binding.petPhoto)
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}