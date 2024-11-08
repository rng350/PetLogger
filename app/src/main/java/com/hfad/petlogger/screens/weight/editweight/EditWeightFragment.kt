package com.hfad.petlogger.screens.weight.editweight

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
import com.hfad.petlogger.common.ConfirmActionUseCase
import com.hfad.petlogger.common.DatePicker
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.screens.pet.petsingleselection.PetSingleSelectionViewModel
import com.hfad.petlogger.R
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.common.TimePicker
import com.hfad.petlogger.databinding.FragmentEditWeightBinding
import com.hfad.petlogger.databinding.FragmentEditWeightDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.pets.usecases.GetAllCheckablePetsUseCase
import com.hfad.petlogger.notes.usecases.GetAllNotesUseCase
import com.hfad.petlogger.tags.usecases.GetAllTagsUseCase
import com.hfad.petlogger.notes.usecases.GetNotesOfWeightUseCase
import com.hfad.petlogger.tags.usecases.GetTagsOfWeightUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.common.setAppBarTitle

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
            EditWeightViewModel.provideFactory(weightRepository, weightId)
        ).get(EditWeightViewModel::class.java)
        binding.viewModel = editWeightViewModel

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getAllPets = GetAllCheckablePetsUseCase(petRepository, initialPetSelection = listOf(petId))
        val petPickerViewModel = ViewModelProvider(this,
            PetSingleSelectionViewModel.provideFactory(getAllPets, petId)
        ).get(PetSingleSelectionViewModel::class.java)
        binding.petPickerViewModel = petPickerViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetAllNotesUseCase(noteRepository)
        val getNotesOfWeight = GetNotesOfWeightUseCase(weightRepository, weightId)
        val noteMultiPickerViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getInitialSelection = getNotesOfWeight
            )
        ).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiPickerViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfWeight = GetTagsOfWeightUseCase(weightRepository, weightId)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags, getTagsOfWeight)
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        setAppBarTitle(getString(R.string.edit_weight_header))

        binding.viewPager.adapter = EditWeightViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                else -> null
            }
        }
        mediator?.attach()

        binding.submitButton.setOnClickListener {
            petPickerViewModel.selectionTracker.currentSelection.value?.item?.petId?.let { petId ->
                editWeightViewModel.submitChanges(
                    petId = petId,
                    notesToAdd = noteMultiPickerViewModel.getNotesToAdd(),
                    notesToRemove = noteMultiPickerViewModel.getNotesToRemove(),
                    tagsToAdd = tagMultiSelectionViewModel.getTagsToAdd(),
                    tagsToRemove = tagMultiSelectionViewModel.getTagsToRemove()
                )
            }
        }
        binding.cancelButton.setOnClickListener{
            findNavController().popBackStack()
        }
        val confirmAction = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_weight_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_weight_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editWeightViewModel.deleteWeight()
            },
            context = requireContext()
        )
        binding.deleteButton.setOnClickListener {
            confirmAction()
        }
        editWeightViewModel.goToWeightsList.observe(viewLifecycleOwner) {
            if (it == true) {
                editWeightViewModel.onNavigateToWeightsList()
                findNavController().navigateSafe(EditWeightFragmentDirections.actionEditWeightFragmentToMonitoringListFragment())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}