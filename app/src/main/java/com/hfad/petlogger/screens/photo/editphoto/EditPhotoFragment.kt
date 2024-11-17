package com.hfad.petlogger.screens.photo.editphoto

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
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.pet.petmultiselection.PetMultiSelectionViewModel
import com.hfad.petlogger.R
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.databinding.FragmentEditPhotoBinding
import com.hfad.petlogger.databinding.FragmentEditPhotoDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.notes.usecases.GetAllNotesUseCase
import com.hfad.petlogger.pets.usecases.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.tags.usecases.GetAllTagsUseCase
import com.hfad.petlogger.events.usecases.GetEventsOfPhotoUseCase
import com.hfad.petlogger.notes.usecases.GetNotesOfPhotoUseCase
import com.hfad.petlogger.pets.usecases.GetPetsOfPhotoUseCase
import com.hfad.petlogger.tags.usecases.GetTagsOfPhotoUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.events.usecases.GetAllEventsUseCase

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

        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val getPetsOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetPetsOfPhotoUseCase(mediaRepository, photoId))
        val petSelectorViewModel = ViewModelProvider(this,
            PetMultiSelectionViewModel.provideFactory(getAllPetsUseCase, getPetsOfPhoto)
        ).get(PetMultiSelectionViewModel::class.java)

        val getALlEvents = GetAllEventsUseCase(database.eventDao)
        val getEventsOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetEventsOfPhotoUseCase(mediaRepository, photoId))
        val eventSelectionViewModel = ViewModelProvider(this,
            EventMultiSelectionViewModel.provideFactory(getAllEvents = getALlEvents, getAssociatedEvents =  getEventsOfPhoto)
        ).get(EventMultiSelectionViewModel::class.java)

        val getAllNotes = GetAllNotesUseCase(noteRepository)
        val getNotesOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetNotesOfPhotoUseCase(mediaRepository, photoId))
        val noteSelectionViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getInitialSelection = getNotesOfPhoto
            )
        ).get(NoteMultiSelectionViewModel::class.java)

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfPhoto = GetMultipleInitialItemsUseCase.PreExisting(GetTagsOfPhotoUseCase(mediaRepository, photoId))
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags, getTagsOfPhoto)
        ).get(TagMultiSelectionViewModel::class.java)

        editPhotoViewModel = ViewModelProvider(this,
            EditPhotoViewModel.provideFactory(mediaRepository, photoId)
        ).get(EditPhotoViewModel::class.java)
        binding.editPhotoViewModel = editPhotoViewModel
        binding.petSelectorViewModel = petSelectorViewModel
        binding.eventSelectorViewModel = eventSelectionViewModel
        binding.noteMultiSelectionViewModel = noteSelectionViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        setAppBarTitle(getString(R.string.editing_photo_details))

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

        binding.submitButton.setOnClickListener{
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
        }

        val confirmAction = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_photo_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_photo_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editPhotoViewModel.deletePhoto() },
            context = requireContext()
        )
        binding.deleteButton.setOnClickListener{
            confirmAction()
        }

        binding.backButton.setOnClickListener{
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