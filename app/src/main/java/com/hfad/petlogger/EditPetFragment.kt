package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentEditPetBinding
import com.hfad.petlogger.photodisplay.stateless.GetCheckableWeightsOfPetUseCase
import com.hfad.petlogger.photodisplay.stateless.GetEventsOfPetUseCase
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

        val petDao = database.petDao
        val photoDao = database.photoDao
        val eventDao = database.eventDao
        val weightDao = database.weightDao

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        editPetViewModel = ViewModelProvider(this, EditPetViewModel.provideFactory(petRepository, petID, petDao, photoDao, eventDao, weightDao)).get(EditPetViewModel::class.java)
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

        editPetViewModel.petProfilePic.observe(viewLifecycleOwner, Observer {
            // if new pfp hasn't been picked yet
            if (editPetViewModel.newPetProfilePic.value == null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.petPhoto)
            }
        })

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
            editPetViewModel.onPetFetched()
        })

        binding.petSexSelection.setOnCheckedChangeListener { radioGroup, i ->
            when(binding.petSexSelection.checkedRadioButtonId) {
                binding.petSexMale.id -> editPetViewModel.setPetSex("Male")
                binding.petSexFemale.id -> editPetViewModel.setPetSex("Female")
                binding.petSexOther.id -> editPetViewModel.setPetSex("Other")
                -1 -> editPetViewModel.setPetSex("")
            }
        }

        binding.inputDOBButton.setOnClickListener {
            DatePicker.generate(editPetViewModel.newPetDOB).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.submit.setOnClickListener {
            editPetViewModel.updatePet(
                eventsToAdd = eventMultiSelectionViewModel.getEventsToAdd(),
                eventsToRemove = eventMultiSelectionViewModel.getEventsToRemove(),
                weightsToRemove = petWeightsDeselectionViewModel.getWeightsToRemove(),
                photosToAdd = mediaSelectionViewModel.getPhotosToAdd(),
                photosToRemove = mediaSelectionViewModel.getPhotosToRemove()
            )
        }

        binding.cancel.setOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.delete.setOnClickListener {
            deleteProfilePicFromLocalStorage()
            this.findNavController().navigate(R.id.action_editPetFragment_to_petListFragment)
        }

        return view
    }

    private fun deleteProfilePicFromLocalStorage() {
        editPetViewModel.petProfilePic.value?.let { photo ->
            photo.contentUri.path?.let { path ->
                File(path).delete()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}