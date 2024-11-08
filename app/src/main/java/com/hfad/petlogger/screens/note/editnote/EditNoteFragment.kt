package com.hfad.petlogger.screens.note.editnote

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
import com.hfad.petlogger.common.ConfirmActionUseCase
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionViewModel
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionFragment
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionViewModel
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionViewModel
import com.hfad.petlogger.R
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.screens.weight.weightmultiselection.WeightMultiSelectionViewModel
import com.hfad.petlogger.databinding.FragmentEditNoteBinding
import com.hfad.petlogger.databinding.FragmentEditNoteDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.pets.usecases.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.tags.usecases.GetAllTagsUseCase
import com.hfad.petlogger.weights.usecases.GetAllWeightsWithPetNamesUseCase
import com.hfad.petlogger.events.usecases.GetEventsOfNoteUseCase
import com.hfad.petlogger.pets.usecases.GetPetsOfNoteUseCase
import com.hfad.petlogger.photos.usecases.GetPhotosOfNoteUseCase
import com.hfad.petlogger.tags.usecases.GetTagsOfNoteUseCase
import com.hfad.petlogger.weights.usecases.GetWeightsOfNoteUseCase
import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.common.setAppBarTitle

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    private lateinit var editNoteViewModel: EditNoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNoteBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val noteId = EditNoteFragmentArgs.fromBundle(requireArguments()).noteId

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val noteRepository = NoteRepository(PetLoggerDatabase.getInstance(requireContext()), mediaRepository)
        editNoteViewModel = ViewModelProvider(this,
            EditNoteViewModel.provideFactory(noteRepository, noteId)
        ).get(EditNoteViewModel::class.java)
        binding.editNoteViewModel = editNoteViewModel

        val eventRepository = EventRepository(database, mediaRepository)
        val getEventsOfNoteUseCase = GetEventsOfNoteUseCase(noteRepository, noteId)
        val eventMultiSelectionViewModel = ViewModelProvider(this,
            EventMultiSelectionViewModel.provideFactory(eventRepository, getEventsOfNoteUseCase)
        ).get(EventMultiSelectionViewModel::class.java)
        binding.eventMultiSelectionViewModel = eventMultiSelectionViewModel

        val petRepository = PetRepository(database, mediaRepository)
        val getAllPets = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val getPetsOfNote = GetPetsOfNoteUseCase(noteRepository, noteId)
        //val getPetsOfNote = null
        val petMultiSelectionViewModel = ViewModelProvider(this,
            PetMultiSelectionViewModel.provideFactory(getAllPets, getPetsOfNote)
        ).get(PetMultiSelectionViewModel::class.java)
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel

        val getPhotosOfNote = GetPhotosOfNoteUseCase(noteRepository, noteId)
        val mediaSelectionViewModel = ViewModelProvider(this,
            MediaSelectionViewModel.provideFactory(mediaRepository, getPhotosOfNote, maxItems = 10)
        ).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val weightRepository = WeightRepository(database)
        val getAllWeights = GetAllWeightsWithPetNamesUseCase(weightRepository)
        val getWeightsOfNote = GetWeightsOfNoteUseCase(noteRepository, noteId)
        val weightMultiSelectionViewModel = ViewModelProvider(this,
            WeightMultiSelectionViewModel.provideFactory(getAllWeights, getWeightsOfNote)
        ).get(WeightMultiSelectionViewModel::class.java)
        binding.weightMultiSelectionViewModel = weightMultiSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfNote = GetTagsOfNoteUseCase(tagRepository, noteId)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(
                tagRepository,
                getAllTags = getAllTags,
                getInitialSelection = getTagsOfNote
            )
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.viewPager.adapter = EditNoteViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
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

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.submitButton.setOnClickListener {
            editNoteViewModel.submitChanges(
                eventsToAdd = eventMultiSelectionViewModel.getEventsToAdd(),
                eventsToRemove = eventMultiSelectionViewModel.getEventsToRemove(),
                petsToAdd = petMultiSelectionViewModel.getPetsToAdd(),
                petsToRemove = petMultiSelectionViewModel.getPetsToRemove(),
                photosToAdd = mediaSelectionViewModel.getPhotosToAdd(),
                photosToRemove = mediaSelectionViewModel.getPhotosToRemove(),
                tagsToAdd = tagMultiSelectionViewModel.getTagsToAdd(),
                tagsToRemove = tagMultiSelectionViewModel.getTagsToRemove()
            )
        }

        val confirmAction = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_note_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_note_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editNoteViewModel.delete()
            },
            context = requireContext()
        )
        binding.deleteButton.setOnClickListener {
            confirmAction()
        }

        editNoteViewModel.fetchedNote.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(title = it.title.ifEmpty { getString(R.string.view_untitled_note_header) }, subtitle = getString(
                    R.string.editing_note_details
                ))
            }
        })

        editNoteViewModel.goBack.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val action = EditNoteFragmentDirections.actionEditNoteFragmentToViewNoteFragment(noteId)
                findNavController().navigateSafe(action)
            }
        })

        editNoteViewModel.goToNotesList.observe(viewLifecycleOwner) {
            if (it == true) {
                editNoteViewModel.onNavigateToNotesList()
                findNavController().navigateSafe(EditNoteFragmentDirections.actionEditNoteFragmentToNoteListFragment())
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

    private class EditNoteViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> EditNoteDetailsFragment()
                1 -> PetMultiSelectionDisplayFragment()
                2 -> EventMultiSelectionDisplayFragment()
                3 -> MediaSelectionFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class EditNoteDetailsFragment : Fragment() {
    private var _binding: FragmentEditNoteDetailsBinding? = null
    val binding: FragmentEditNoteDetailsBinding get() = _binding!!
    private val editNoteViewModel: EditNoteViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNoteDetailsBinding.inflate(inflater, container, false)
        binding.editNoteViewModel = editNoteViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}