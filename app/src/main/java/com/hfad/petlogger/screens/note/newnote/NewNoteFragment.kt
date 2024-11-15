package com.hfad.petlogger.screens.note.newnote

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionViewModel
import com.hfad.petlogger.R
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.screens.weight.weightmultiselection.WeightMultiSelectionViewModel
import com.hfad.petlogger.databinding.FragmentNewNoteBinding
import com.hfad.petlogger.databinding.FragmentNewNoteDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.pets.usecases.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.tags.usecases.GetAllTagsUseCase
import com.hfad.petlogger.weights.usecases.GetAllWeightsWithPetNamesUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionViewModel
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionFragment
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionViewModel
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.util.Constants.Companion.defaultNullIdForNavigation
import com.hfad.petlogger.events.usecases.GetAllEventsUseCase
import com.hfad.petlogger.events.usecases.GetSingleEventUseCase
import com.hfad.petlogger.pets.usecases.GetSinglePetUseCase
import com.hfad.petlogger.photos.usecases.GetSinglePhotoUseCase
import com.hfad.petlogger.tags.usecases.GetSingleTagUseCase
import com.hfad.petlogger.weights.usecases.GetSingleWeightUseCase

class NewNoteFragment : Fragment() {
    private var _binding: FragmentNewNoteBinding? = null
    val binding get() = _binding!!
    private lateinit var newNoteViewModel: NewNoteViewModel
    private lateinit var petMultiSelectionViewModel: PetMultiSelectionViewModel
    private lateinit var eventMultiSelectionViewModel: EventMultiSelectionViewModel
    private lateinit var weightMultiSelectionViewModel: WeightMultiSelectionViewModel
    private lateinit var mediaSelectionViewModel: MediaSelectionViewModel
    private lateinit var tagMultiSelectionViewModel: TagMultiSelectionViewModel
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(database, mediaRepository)
        val weightRepository = WeightRepository(database)

        setAppBarTitle(getString(R.string.new_note_header))

        binding.viewPager.adapter = NewNoteViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
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

        newNoteViewModel = ViewModelProvider(this, NewNoteViewModel.provideFactory(noteRepository)).get(
            NewNoteViewModel::class.java)

        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val assocPetId = NewNoteFragmentArgs.fromBundle(requireArguments()).petId
        val getNewAssociatedPet = if (assocPetId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSinglePetUseCase(database.petDao, assocPetId))
        } else null
        petMultiSelectionViewModel = ViewModelProvider(this,
            PetMultiSelectionViewModel.provideFactory(getAllPets = getAllPetsUseCase, getInitialSelection = getNewAssociatedPet)
        ).get(PetMultiSelectionViewModel::class.java)

        val getAllEvents = GetAllEventsUseCase(database.eventDao)
        val assocEventId = NewNoteFragmentArgs.fromBundle(requireArguments()).eventId
        val getAssociatedEvent = if (assocEventId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSingleEventUseCase(database.eventDao, assocEventId))
        } else null
        eventMultiSelectionViewModel = ViewModelProvider(this,
            EventMultiSelectionViewModel.provideFactory(getAllEvents = getAllEvents, getAssociatedEvents = getAssociatedEvent)
        ).get(EventMultiSelectionViewModel::class.java)

        val getAllWeights = GetAllWeightsWithPetNamesUseCase(weightRepository)
        val assocWeightId = NewNoteFragmentArgs.fromBundle(requireArguments()).weightId
        val getAssociatedWeight = if (assocWeightId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSingleWeightUseCase(database.weightDao, assocWeightId))
        } else null
        weightMultiSelectionViewModel = ViewModelProvider(this,
            WeightMultiSelectionViewModel.provideFactory(getAllWeights, getAssociatedWeight)
        ).get(WeightMultiSelectionViewModel::class.java)

        val assocPhotoId = NewNoteFragmentArgs.fromBundle(requireArguments()).photoId
        val getAssociatedPhoto = if (assocPhotoId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSinglePhotoUseCase(database.photoDao, assocPhotoId))
        } else null
        mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(mediaRepository = mediaRepository, fetchInitialSelection = getAssociatedPhoto, maxItems = 10)).get(
            MediaSelectionViewModel::class.java)

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val assocTagId = NewNoteFragmentArgs.fromBundle(requireArguments()).tagId
        val getAssociatedTag = if (assocTagId != defaultNullIdForNavigation) {
            GetMultipleInitialItemsUseCase.New(GetSingleTagUseCase(database.tagDao, assocTagId))
        } else null
        tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags, getAssociatedTag)
        ).get(TagMultiSelectionViewModel::class.java)

        binding.newNoteViewModel = newNoteViewModel
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel
        binding.eventMultiSelectionViewModel = eventMultiSelectionViewModel
        binding.weightMultiSelectionViewModel = weightMultiSelectionViewModel
        binding.mediaSelectionViewModel = mediaSelectionViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel


        binding.lifecycleOwner = viewLifecycleOwner

        binding.submitButton.setOnClickListener {
            newNoteViewModel.submitNote(
                pets = petMultiSelectionViewModel.getPetsToAdd(),
                events = eventMultiSelectionViewModel.getEventsToAdd(),
                weights = weightMultiSelectionViewModel.getWeightsToAdd(),
                photos = mediaSelectionViewModel.getPhotosToAdd(),
                tags = tagMultiSelectionViewModel.getTagsToAdd()
            )
        }

        binding.clearButton.setOnClickListener {
            newNoteViewModel.clear()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        newNoteViewModel.goBack.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigateSafe(NewNoteFragmentDirections.actionNewNoteFragmentToNoteListFragment())
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

    private class NewNoteViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> NewNoteDetailsFragment()
                1 -> PetMultiSelectionDisplayFragment()
                2 -> EventMultiSelectionDisplayFragment()
                3 -> MediaSelectionFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class NewNoteDetailsFragment() : Fragment() {
    private var _binding: FragmentNewNoteDetailsBinding? = null
    val binding: FragmentNewNoteDetailsBinding get() = _binding!!
    private val newNoteViewModel: NewNoteViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewNoteDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        binding.newNoteViewModel = newNoteViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}