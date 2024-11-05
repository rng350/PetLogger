package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.databinding.FragmentEditPetBinding
import com.hfad.petlogger.databinding.FragmentEditPetDetailsBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllNotesUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetCheckableWeightsOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetEventsOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetNotesOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPetProfilePhotoUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPhotosOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetTagsOfPetUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.TagRepository

class EditPetFragment : Fragment() {
    private var _binding: FragmentEditPetBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null
    lateinit var editPetViewModel: EditPetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val petID = EditPetFragmentArgs.fromBundle(requireArguments()).petId

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        editPetViewModel = ViewModelProvider(this, EditPetViewModel.provideFactory(petRepository, petID)).get(EditPetViewModel::class.java)
        binding.editPetViewModel = editPetViewModel

        val getPetWeights = GetCheckableWeightsOfPetUseCase(petRepository, petID)
        val petWeightsDeselectionViewModel = ViewModelProvider(this, PetWeightDeselectionViewModel.provideFactory(getPetWeights)).get(PetWeightDeselectionViewModel::class.java)
        binding.petWeightDeselectionViewModel = petWeightsDeselectionViewModel

        editPetViewModel.pet.observe(viewLifecycleOwner, Observer {
            it?.let {
                val mainActivity = (activity as MainActivity)
                mainActivity.setTopAppBarTitle(it.petName)
                mainActivity.setTopAppBarSubtitle(getString(R.string.editing_pet_details))
            }
        })

        val getEventsOfPet = GetEventsOfPetUseCase(petRepository = petRepository, petId = petID)
        val eventRepository = EventRepository(database, mediaRepository)
        val eventMultiSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository, getEventsOfPet)).get(EventMultiSelectionViewModel::class.java)
        binding.eventMultiSelectionViewModel = eventMultiSelectionViewModel

        val getPhotosOfPet = GetPhotosOfPetUseCase(petRepository = petRepository, petId = petID)
        val mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(mediaRepository, getPhotosOfPet)).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val getPetProfilePhoto = GetPetProfilePhotoUseCase(petRepository, petID)
        val petProfilePhotoSelectionViewModel = ViewModelProvider(this, MediaSingleSelectionViewModel.provideFactory(mediaRepository, getPetProfilePhoto)).get(MediaSingleSelectionViewModel::class.java)
        binding.petProfilePhotoSelectionViewModel = petProfilePhotoSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetAllNotesUseCase(noteRepository)
        val getNotesOfPet = GetNotesOfPetUseCase(petRepository, petID)
        val noteSelectionViewModel = ViewModelProvider(this, NoteMultiSelectionViewModel.provideFactory(getAllNotes, getNotesOfPet)).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getTagsOfPet = GetTagsOfPetUseCase(petRepository, petID)
        val tagMultiSelectionViewModel = ViewModelProvider(this, TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags, getTagsOfPet)).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

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

        binding.submit.setOnClickListener {
            if (editPetViewModel.petName.value!!.isNotEmpty()) {
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
        }

        binding.cancel.setOnClickListener {
            this.findNavController().popBackStack()
        }

        val confirmAction = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_pet_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_pet_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editPetViewModel.deletePet() },
            context = requireContext()
        )
        binding.delete.setOnClickListener {
            confirmAction()
        }

        editPetViewModel.doneUpdating.observe(viewLifecycleOwner) {isDoneUpdating ->
            if (isDoneUpdating) {
                findNavController().navigateSafe(EditPetFragmentDirections.actionEditPetFragmentToViewPetFragment(petID))
                editPetViewModel.wentBack()
            }
        }

        editPetViewModel.goToPetList.observe(viewLifecycleOwner) {shouldGo ->
            if (shouldGo) {
                this.findNavController().navigateSafe(EditPetFragmentDirections.actionEditPetFragmentToPetListFragment())
                editPetViewModel.wentToPetList()
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
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.editPetViewModel = editPetViewModel
        // initialize sex pick
        editPetViewModel.pet.observeOnce(viewLifecycleOwner, Observer {
            when(it.petSex) {
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
        })

        binding.petSexSelection.setOnCheckedChangeListener { radioGroup, i ->
            when(binding.petSexSelection.checkedRadioButtonId) {
                binding.petSexMale.id -> editPetViewModel.setPetSex("Male")
                binding.petSexFemale.id -> editPetViewModel.setPetSex("Female")
                binding.petSexOther.id -> editPetViewModel.setPetSex("Other")
                -1 -> editPetViewModel.setPetSex("")
            }
        }

        binding.addPetBirthDateButton.setOnClickListener {
            DatePicker.generate(editPetViewModel.newPetDOB).show(parentFragmentManager, "DATE_PICKER")
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}