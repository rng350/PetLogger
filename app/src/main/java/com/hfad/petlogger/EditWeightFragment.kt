package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditWeightBinding
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.photodisplay.stateless.GetAllCheckablePetsUseCase
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository

class EditWeightFragment : Fragment() {
    private var _binding: FragmentEditWeightBinding? = null
    val binding get() = _binding!!
    lateinit var petPickerViewModel: PetSingleSelectionViewModel

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
        val weightDao = database.weightDao

        val editWeightViewModelFactory = EditWeightViewModelFactory(weightId, weightDao)
        val editWeightViewModel = ViewModelProvider(this, editWeightViewModelFactory).get(EditWeightViewModel::class.java)
        binding.viewModel = editWeightViewModel

        val petDao = PetLoggerDatabase.getInstance(application).petDao

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getAllPets = GetAllCheckablePetsUseCase(petRepository, listOf(weightId))

        editWeightViewModel.pet.observeOnce(viewLifecycleOwner) {
            it?.let {
                petPickerViewModel = ViewModelProvider(
                    this,
                    PetSingleSelectionViewModel.provideFactory(getAllPets, weightId)
                )[PetSingleSelectionViewModel::class.java]
            }
        }

        setAppBarTitle(getString(R.string.edit_weight_header))

        binding.changeAssocPetButton.setOnClickListener{
            PetSingleSelectionDialogFragment().show(childFragmentManager, "PET_SINGLE_PICKER")
        }

        binding.weightDate.setOnClickListener{
            DatePicker.generate(editWeightViewModel.weightDateTime).show(parentFragmentManager, "DATEPICKER")
        }

        binding.weightTime.setOnClickListener{
            TimePicker.generate(editWeightViewModel.weightDateTime, requireContext()).show(parentFragmentManager, "TIMEPICKER")
        }
        binding.submitButton.setOnClickListener {
            editWeightViewModel.submitChanges()
        }
        binding.cancelButton.setOnClickListener{
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}