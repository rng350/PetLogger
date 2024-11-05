package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.databinding.FragmentNewNoteBinding
import com.hfad.petlogger.databinding.FragmentNewNoteDetailsBinding
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.photodisplay.stateless.GetAllCheckablePetsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllWeightsWithPetNamesUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.TagRepository
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.selectiontracker.MultiSelectionTracker

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
        val eventRepository = EventRepository(database, mediaRepository)
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

        newNoteViewModel = ViewModelProvider(this, NewNoteViewModel.provideFactory(noteRepository)).get(NewNoteViewModel::class.java)

        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        petMultiSelectionViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(getAllPetsUseCase)).get(PetMultiSelectionViewModel::class.java)
        eventMultiSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository)).get(EventMultiSelectionViewModel::class.java)

        val getAllWeights = GetAllWeightsWithPetNamesUseCase(weightRepository)
        weightMultiSelectionViewModel = ViewModelProvider(this, WeightMultiSelectionViewModel.provideFactory(getAllWeights)).get(WeightMultiSelectionViewModel::class.java)
        mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(mediaRepository = mediaRepository, maxItems = 10)).get(MediaSelectionViewModel::class.java)

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        tagMultiSelectionViewModel = ViewModelProvider(this, TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags)).get(TagMultiSelectionViewModel::class.java)

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
        binding.newNoteViewModel = newNoteViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}