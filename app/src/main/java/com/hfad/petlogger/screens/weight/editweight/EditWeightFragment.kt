package com.hfad.petlogger.screens.weight.editweight

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.usecases.ConfirmActionUseCase
import com.hfad.petlogger.common.datetimeselection.DatePicker
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.screens.pet.petsingleselection.PetSingleSelectionViewModel
import com.hfad.petlogger.R
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.common.datetimeselection.TimePicker
import com.hfad.petlogger.databinding.FragmentEditWeightBinding
import com.hfad.petlogger.databinding.FragmentEditWeightDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsUseCase
import com.hfad.petlogger.notes.domain.usecases.GetNotesOfWeightUseCase
import com.hfad.petlogger.tags.domain.usecases.GetTagsOfWeightUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.tags.domain.TagRepository
import com.hfad.petlogger.weights.domain.WeightRepository
import com.hfad.petlogger.common.usecases.GetSingleInitialItemUseCase
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.usecases.GetPossessiveFormUseCase
import com.hfad.petlogger.notes.domain.usecases.GetAllNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetSearchedNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.pets.domain.usecases.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.pets.domain.usecases.GetSinglePetUseCase
import com.hfad.petlogger.tags.domain.usecases.GetAllTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.domain.usecases.GetSearchedTagsUseCase
import com.hfad.petlogger.weights.domain.usecases.GetSingleWeightUseCase

class EditWeightFragment : Fragment() {
    private var _binding: FragmentEditWeightBinding? = null
    val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditWeightBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val weightId = EditWeightFragmentArgs.fromBundle(requireArguments()).weightId
        val petId = EditWeightFragmentArgs.fromBundle(requireArguments()).petId
        val weightRepository = WeightRepository(database)
        val editWeightViewModel = ViewModelProvider(this,
            EditWeightViewModel.provideFactory(weightRepository, GetSingleWeightUseCase(database.weightDao, weightId))
        ).get(EditWeightViewModel::class.java)
        binding.viewModel = editWeightViewModel

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getAllPets = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val getExistingPet = GetSingleInitialItemUseCase.PreExisting<PetWithProfilePic>(
            GetSinglePetUseCase(database.petDao, petId)
        )
        val petPickerViewModel = ViewModelProvider(this,
            PetSingleSelectionViewModel.provideFactory(getAllPets, getExistingPet)
        ).get(PetSingleSelectionViewModel::class.java)
        binding.petPickerViewModel = petPickerViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, noteAmt = 10)
        val getNotesOfWeight = GetMultipleInitialItemsUseCase.PreExisting(GetNotesOfWeightUseCase(weightRepository, weightId))
        val getSearchedNotesFromAll = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10)
        val getAllNotesFromCurrentSelectionFactory = GetAllNotesFromCurrentSelectionUseCaseFactory()
        val getSearchedNotesFromCurrentSelectionFactory = GetSearchedNotesFromCurrentSelectionUseCaseFactory(database.noteDao)
        val noteMultiPickerViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getInitialSelection = getNotesOfWeight,
                getSearchedSelectionOptions = getSearchedNotesFromAll,
                getAllNotesFromCurrentSelectionFactory = getAllNotesFromCurrentSelectionFactory,
                getSearchedNotesFromCurrentSelectionFactory = getSearchedNotesFromCurrentSelectionFactory
            )
        ).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiPickerViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfWeight = GetMultipleInitialItemsUseCase.PreExisting(GetTagsOfWeightUseCase(weightRepository, weightId))
        val getSearchedTagsFromAll = GetSearchedTagsUseCase(tagRepository)
        val getAllTagsFromCurrentSelectionFactory = GetAllTagsFromCurrentSelectionUseCaseFactory()
        val getSearchedTagsFromCurrentSelectionFactory = GetSearchedTagsFromCurrentSelectionUseCaseFactory(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(
                getAllTags = getAllTags,
                getAllSearchedTagsUseCase = getSearchedTagsFromAll,
                getAllCurrentSelectionFactory = getAllTagsFromCurrentSelectionFactory,
                getSearchedTagsFromCurrentSelectionFactory = getSearchedTagsFromCurrentSelectionFactory,
                getInitialSelection = getTagsOfWeight
            )
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        editWeightViewModel.weightPetName.observe(viewLifecycleOwner) { petName ->
            petName?.let {
                val getPossessiveForm = GetPossessiveFormUseCase()
                binding.editWeightTopAppBar.title = getString(R.string.editing_pet_weight, getPossessiveForm(petName))
            }
        }
        editWeightViewModel.initWeightDateTimeDisplay.observe(viewLifecycleOwner) { initWeightDateTimeDisplay ->
            initWeightDateTimeDisplay?.let {
                binding.editWeightTopAppBar.subtitle = it
            }
        }

        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.adapter = EditWeightViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                else -> null
            }
        }
        mediator?.attach()

        binding.editWeightTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        val confirmDelete = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_weight_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_weight_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editWeightViewModel.deleteWeight()
            },
            context = requireContext()
        )
        binding.editWeightTopAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.delete -> {
                    confirmDelete()
                    true
                }
                R.id.submit -> {
                    petPickerViewModel.selectionTracker.currentSelection.value?.petId?.let { petId ->
                        editWeightViewModel.submitChanges(
                            petId = petId,
                            notesToAdd = noteMultiPickerViewModel.getNotesToAdd(),
                            notesToRemove = noteMultiPickerViewModel.getNotesToRemove(),
                            tagsToAdd = tagMultiSelectionViewModel.getTagsToAdd(),
                            tagsToRemove = tagMultiSelectionViewModel.getTagsToRemove()
                        )
                    }
                    true
                }
                else -> false
            }
        }
        editWeightViewModel.goToWeightsList.observe(viewLifecycleOwner) {
            if (it == true) {
                editWeightViewModel.onNavigateToWeightsList()
                findNavController().navigateSafe(EditWeightFragmentDirections.actionEditWeightFragmentToMonitoringListFragment())
            }
        }
        editWeightViewModel.goToViewWeight.observe(viewLifecycleOwner) {
            if (it == true) {
                editWeightViewModel.onNavigateToViewWeight()
                findNavController().navigateSafe(EditWeightFragmentDirections.actionEditWeightFragmentToViewWeightFragment(weightId))
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

    private class EditWeightViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> EditWeightDetailsFragment()
                1 -> NoteMultiSelectionDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class EditWeightDetailsFragment() : Fragment() {
    private var _binding: FragmentEditWeightDetailsBinding? = null
    val binding: FragmentEditWeightDetailsBinding get() = _binding!!
    private val editWeightViewModel: EditWeightViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditWeightDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.viewModel = editWeightViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.weightDate.setOnClickListener{
            DatePicker.generate(editWeightViewModel.weightDateTime).show(parentFragmentManager, "DATEPICKER")
        }

        binding.weightTime.setOnClickListener{
            TimePicker.generate(editWeightViewModel.weightDateTime, requireContext())
                .show(parentFragmentManager, "TIMEPICKER")
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val items = listOf("grams", "kilograms", "pounds", "ounces")

        val arrayAdapter = ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, items)
        binding.weightUnitDropDownList.setAdapter(arrayAdapter)
        binding.weightUnitDropDownList.setText(editWeightViewModel.unitType, false)
        arrayAdapter.notifyDataSetChanged()

        binding.weightUnitDropDownList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            editWeightViewModel.setWeightUnitType(parent.getItemAtPosition(position).toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}