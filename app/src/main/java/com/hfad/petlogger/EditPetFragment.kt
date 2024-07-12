package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hfad.petlogger.databinding.FragmentEditPetBinding
import com.hfad.petlogger.photodisplay.stateless.GetCheckableWeightsOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetEventsOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPetProfilePhotoUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPhotosOfPetUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository
import java.io.File

class EditPetFragment : Fragment() {
    private var _binding: FragmentEditPetBinding? = null
    private val binding get() = _binding!!
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

        binding.submit.setOnClickListener {
            if (editPetViewModel.petName.value!!.isNotEmpty()) {
                editPetViewModel.updatePet(
                    eventsToAdd = eventMultiSelectionViewModel.getEventsToAdd(),
                    eventsToRemove = eventMultiSelectionViewModel.getEventsToRemove(),
                    weightsToRemove = petWeightsDeselectionViewModel.getWeightsToRemove(),
                    photosToAdd = mediaSelectionViewModel.getPhotosToAdd(),
                    photosToRemove = mediaSelectionViewModel.getPhotosToRemove(),
                    petProfilePhotoToAdd = if (petProfilePhotoSelectionViewModel.photoToAdd.isNotEmpty()) petProfilePhotoSelectionViewModel.photoToAdd[0] else null,
                    petProfilePhotoToRemove = if (petProfilePhotoSelectionViewModel.photoToRemove.isNotEmpty()) petProfilePhotoSelectionViewModel.photoToRemove[0] else null
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
        _binding = null
    }
}