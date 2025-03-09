package com.hfad.petlogger.screens.pet.editpet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.R
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.common.datetimeselection.DatePicker
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.common.selectiontracker.MultiDeselectionDisplay
import com.hfad.petlogger.common.usecases.ConfirmActionUseCase
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.databinding.FragmentEditPetBinding
import com.hfad.petlogger.databinding.FragmentEditPetDetailsBinding
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.events.domain.usecases.GetAllEventsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.events.domain.usecases.GetEventsOfPetUseCase
import com.hfad.petlogger.events.domain.usecases.GetMoreOfAllEventsUseCase
import com.hfad.petlogger.events.domain.usecases.GetMoreOfSearchedEventsUseCase
import com.hfad.petlogger.events.domain.usecases.GetSearchedEventsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.notes.domain.usecases.GetAllNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetNotesOfPetUseCase
import com.hfad.petlogger.notes.domain.usecases.GetSearchedNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.pets.domain.usecases.GetPetDetailsForEditUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.photos.domain.usecases.BuildPhotoSearchQueryUseCase
import com.hfad.petlogger.photos.domain.usecases.GetMoreOfSearchedPhotosUseCase
import com.hfad.petlogger.photos.domain.usecases.GetMorePhotosOfPetUseCase
import com.hfad.petlogger.photos.domain.usecases.GetPetProfilePhotoUseCase
import com.hfad.petlogger.photos.domain.usecases.GetPhotosOfPetUseCase
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.event.eventmultiselection.EventMultiSelectionViewModel
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionFragment
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionViewModel
import com.hfad.petlogger.screens.photo.mediaselection.MediaSingleSelectionViewModel
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.screens.weight.PetWeightDeselectionFragment
import com.hfad.petlogger.screens.weight.PetWeightDeselectionViewModel
import com.hfad.petlogger.tags.domain.TagRepository
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsUseCase
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsUseCase
import com.hfad.petlogger.tags.domain.usecases.GetTagsOfPetUseCase
import com.hfad.petlogger.weights.data.PetWeightForSelection
import com.hfad.petlogger.weights.domain.usecases.GetAllWeightsOfPetForSelectionUseCase
import com.hfad.petlogger.weights.domain.usecases.GetSearchedPetWeightsForSelectionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditPetFragment : Fragment() {
    private var _binding: FragmentEditPetBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null
    private lateinit var editPetViewModel: EditPetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val petId = EditPetFragmentArgs.fromBundle(requireArguments()).petId

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getPetDetailsForEdit = GetPetDetailsForEditUseCase(database.petDao, petId)
        editPetViewModel = ViewModelProvider(this,
            EditPetViewModel.provideFactory(petId, petRepository, getPetDetailsForEdit)
        ).get(EditPetViewModel::class.java)
        binding.editPetViewModel = editPetViewModel

        val getAllPetWeightsForSelection = GetAllWeightsOfPetForSelectionUseCase(database.weightDao, petId, weightsAmt = 10)
        val getSearchedPetWeightsForSelection = GetSearchedPetWeightsForSelectionUseCase(database.weightDao, weightsAmt = 10, petId)
        val getPetWeightDeselectionDisplay = MultiDeselectionDisplay<PetWeightForSelection>(
            getAllAssociatedItems = getAllPetWeightsForSelection,
            getSearchedItems = getSearchedPetWeightsForSelection,
            coroutineScope = editPetViewModel.viewModelScope
        )
        val petWeightsDeselectionViewModel = ViewModelProvider(this,
            PetWeightDeselectionViewModel.provideFactory(getPetWeightDeselectionDisplay)
        ).get(PetWeightDeselectionViewModel::class.java)
        binding.petWeightDeselectionViewModel = petWeightsDeselectionViewModel

        val eventRepository = EventRepository(database, mediaRepository)
        val getAllEvents = GetMoreOfAllEventsUseCase(eventRepository, eventAmt = 10)
        val getEventsOfPet = GetMultipleInitialItemsUseCase.PreExisting(GetEventsOfPetUseCase(petRepository = petRepository, petId = petId))
        val getSearchedEvents = GetMoreOfSearchedEventsUseCase(database.eventDao, eventAmt = 10)
        val getAllEventsFromCurrentSelectionFactory = GetAllEventsFromCurrentSelectionUseCaseFactory()
        val getSearchedEventsFromCurrentSelectionFactory = GetSearchedEventsFromCurrentSelectionUseCaseFactory(database.eventDao)
        val eventMultiSelectionViewModel = ViewModelProvider(this,
            EventMultiSelectionViewModel.provideFactory(
                getAllEvents = getAllEvents,
                getAssociatedEvents = getEventsOfPet,
                getSearchedEvents = getSearchedEvents,
                getAllEventsFromCurrentSelection = getAllEventsFromCurrentSelectionFactory,
                getSearchedEventsFromCurrentSelectionFactory = getSearchedEventsFromCurrentSelectionFactory
            )
        ).get(EventMultiSelectionViewModel::class.java)
        binding.eventMultiSelectionViewModel = eventMultiSelectionViewModel

        val getPhotosOfPet = GetMultipleInitialItemsUseCase.PreExisting(GetPhotosOfPetUseCase(petRepository = petRepository, petId = petId))
        val getMorePhotosOfPet = GetMorePhotosOfPetUseCase(petRepository, petId, photosAmt = 10)
        val getSearchedPhotosOfPet = GetMoreOfSearchedPhotosUseCase(database.photoDao, photosAmt = 10, pickFrom = BuildPhotoSearchQueryUseCase.Pick.FromPet(petId))
        val mediaSelectionViewModel = ViewModelProvider(this,
            MediaSelectionViewModel.provideFactory(
                mediaRepository = mediaRepository,
                getInitialSelection = getPhotosOfPet,
                getAssociatedItems = getMorePhotosOfPet,
                getSearchedPhotos = getSearchedPhotosOfPet
            )
        ).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val getPetProfilePhoto = GetPetProfilePhotoUseCase(petRepository, petId)
        val petProfilePhotoSelectionViewModel = ViewModelProvider(this,
            MediaSingleSelectionViewModel.provideFactory(mediaRepository, getPetProfilePhoto)
        ).get(MediaSingleSelectionViewModel::class.java)
        binding.petProfilePhotoSelectionViewModel = petProfilePhotoSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, noteAmt = 10)
        val getNotesOfPet = GetMultipleInitialItemsUseCase.PreExisting(GetNotesOfPetUseCase(petRepository, petId))
        val getSearchedNotesFromAll = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10)
        val getAllNotesFromCurrentSelectionFactory = GetAllNotesFromCurrentSelectionUseCaseFactory()
        val getSearchedNotesFromCurrentSelectionFactory = GetSearchedNotesFromCurrentSelectionUseCaseFactory(database.noteDao)
        val noteSelectionViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getInitialSelection = getNotesOfPet,
                getSearchedSelectionOptions = getSearchedNotesFromAll,
                getAllNotesFromCurrentSelectionFactory = getAllNotesFromCurrentSelectionFactory,
                getSearchedNotesFromCurrentSelectionFactory = getSearchedNotesFromCurrentSelectionFactory
            )
        ).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfPet = GetMultipleInitialItemsUseCase.PreExisting(GetTagsOfPetUseCase(petRepository, petId))
        val getSearchedTagsFromAll = GetSearchedTagsUseCase(tagRepository)
        val getAllTagsFromCurrentSelectionFactory = GetAllTagsFromCurrentSelectionUseCaseFactory()
        val getSearchedTagsFromCurrentSelectionFactory = GetSearchedTagsFromCurrentSelectionUseCaseFactory(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(
                getAllTags = getAllTags,
                getAllSearchedTagsUseCase = getSearchedTagsFromAll,
                getAllCurrentSelectionFactory = getAllTagsFromCurrentSelectionFactory,
                getSearchedTagsFromCurrentSelectionFactory = getSearchedTagsFromCurrentSelectionFactory,
                getInitialSelection = getTagsOfPet
            )
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.viewPager.offscreenPageLimit = 5
        binding.viewPager.adapter = EditPetViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.weights)
                2 -> getString(R.string.events)
                3 -> getString(R.string.notes)
                4 -> getString(R.string.photos_header)
                else -> null
            }
        }
        mediator?.attach()

        val confirmDelete = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_pet_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_pet_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editPetViewModel.deletePet() },
            context = requireContext()
        )
        binding.editPetTopAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.delete -> {
                    confirmDelete()
                    true
                }
                R.id.submit -> {
                    if (editPetViewModel.petName.value.isNotEmpty()) {
                        editPetViewModel.updatePet(
                            eventsToAdd = eventMultiSelectionViewModel.getEventsToAdd(),
                            eventsToRemove = eventMultiSelectionViewModel.getEventsToRemove(),
                            weightsToRemove = petWeightsDeselectionViewModel.getWeightsToRemove(),
                            photosToAdd = mediaSelectionViewModel.getPhotosToAdd(),
                            photosToRemove = mediaSelectionViewModel.getPhotosToRemove(),
                            petProfilePhotoToAdd = if (petProfilePhotoSelectionViewModel.photoToAdd.isNotEmpty()) petProfilePhotoSelectionViewModel.photoToAdd[0] else null,
                            petProfilePhotoToRemove = if (petProfilePhotoSelectionViewModel.photoToRemove.isNotEmpty()) petProfilePhotoSelectionViewModel.photoToRemove[0] else null,
                            notesToAdd = noteSelectionViewModel.getNotesToAdd(),
                            notesToRemove = noteSelectionViewModel.getNotesToRemove(),
                            tagsToAdd = tagMultiSelectionViewModel.getTagsToAdd(),
                            tagsToRemove = tagMultiSelectionViewModel.getTagsToRemove()
                        )
                    } else Toast.makeText(requireContext(), R.string.no_pet_name_given, Toast.LENGTH_LONG).show()
                    true
                }
                else -> false
            }
        }
        binding.editPetTopAppBar.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    editPetViewModel.doneUpdating.collectLatest { doneUpdating ->
                        if (doneUpdating) {
                            findNavController().navigateSafe(EditPetFragmentDirections.actionEditPetFragmentToViewPetFragment(petId))
                            editPetViewModel.wentBack()
                        }
                    }
                }
                launch {
                    editPetViewModel.goToPetList.collectLatest { shouldGo ->
                        if (shouldGo) {
                            findNavController().navigateSafe(EditPetFragmentDirections.actionEditPetFragmentToPetListFragment())
                            editPetViewModel.wentToPetList()
                        }
                    }
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

    private class EditPetViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 5
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> EditPetDetailsFragment()
                1 -> PetWeightDeselectionFragment()
                2 -> EventMultiSelectionDisplayFragment()
                3 -> NoteMultiSelectionDisplayFragment()
                4 -> MediaSelectionFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class EditPetDetailsFragment : Fragment() {
    private var _binding: FragmentEditPetDetailsBinding? = null
    val binding: FragmentEditPetDetailsBinding get() = _binding!!
    private val editPetViewModel: EditPetViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        binding.editPetViewModel = editPetViewModel

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    editPetViewModel.petSex.collectLatest {
                        when(it) {
                            "Male" -> {
                                binding.petSexMale.isChecked = true
                            }
                            "Female" -> {
                                binding.petSexFemale.isChecked = true
                            }
                            "Other" -> {
                                binding.petSexOther.isChecked = true
                            }
                        }
                    }
                }
                launch {
                    editPetViewModel.petStatus.collectLatest {
                        when (it) {
                            EditPetViewModel.PetStatus.Active -> {
                                binding.petDateOfPassingLayout.visibility = View.GONE
                                binding.petStatusDropDown.setText("Active", false)
                            }
                            EditPetViewModel.PetStatus.PassedAway -> {
                                binding.petDateOfPassingLayout.visibility = View.VISIBLE
                                binding.petStatusDropDown.setText("Passed Away", false)
                            }
                        }
                    }
                }
            }
        }

        binding.petSexSelection.setOnCheckedChangeListener { radioGroup, i ->
            when(binding.petSexSelection.checkedRadioButtonId) {
                binding.petSexMale.id -> editPetViewModel.setPetSex("Male")
                binding.petSexFemale.id -> editPetViewModel.setPetSex("Female")
                binding.petSexOther.id -> editPetViewModel.setPetSex("Other")
                -1 -> editPetViewModel.setPetSex("")
            }
        }

        binding.petBirthDateDisplay.setOnClickListener {
            binding.petBirthDateDisplay.isEnabled = false
            CoroutineScope(Dispatchers.Main.immediate).launch {
                DatePicker
                    .generate(editPetViewModel.newPetDOB)
                    .show(parentFragmentManager, "DATE_OF_BIRTH_PICKER")
                delay(200)
                binding.petBirthDateDisplay.isEnabled = true
            }
        }

        binding.petDateOfPassingDisplay.setOnClickListener {
            binding.petDateOfPassingDisplay.isEnabled = false
            CoroutineScope(Dispatchers.Main.immediate).launch {
                DatePicker
                    .generate(editPetViewModel.newPetDateOfPassing)
                    .show(parentFragmentManager, "DATE_OF_PASSING_PICKER")
                delay(200)
                binding.petDateOfPassingDisplay.isEnabled = true
            }
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        binding.petBirthDateDisplay.isEnabled = true
        binding.petDateOfPassingDisplay.isEnabled = true
    }

    override fun onResume() {
        super.onResume()

        val statusOptions = listOf("Active", "Passed Away")
        val adapter = ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, statusOptions)
        binding.petStatusDropDown.setAdapter(adapter)
        adapter.notifyDataSetChanged()
        binding.petStatusDropDown.setOnItemClickListener { _, _, position, _ ->
            if (position == 1) {
                editPetViewModel.setPetStatus(EditPetViewModel.PetStatus.PassedAway)
            } else {
                editPetViewModel.setPetStatus(EditPetViewModel.PetStatus.Active)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}