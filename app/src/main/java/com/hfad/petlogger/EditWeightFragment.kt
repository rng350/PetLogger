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
import com.hfad.petlogger.repositories.WeightRepository

class EditWeightFragment : Fragment() {
    private var _binding: FragmentEditWeightBinding? = null
    val binding get() = _binding!!

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
        val editWeightViewModel = ViewModelProvider(this, EditWeightViewModel.provideFactory(weightRepository, weightId)).get(EditWeightViewModel::class.java)
        binding.viewModel = editWeightViewModel

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getAllPets = GetAllCheckablePetsUseCase(petRepository, initialPetSelection = listOf(petId))
        val petPickerViewModel = ViewModelProvider(this, PetSingleSelectionViewModel.provideFactory(getAllPets, petId)).get(PetSingleSelectionViewModel::class.java)
        binding.petPickerViewModel = petPickerViewModel

        setAppBarTitle(getString(R.string.edit_weight_header))

        binding.weightDate.setOnClickListener{
            DatePicker.generate(editWeightViewModel.weightDateTime).show(parentFragmentManager, "DATEPICKER")
        }

        binding.weightTime.setOnClickListener{
            TimePicker.generate(editWeightViewModel.weightDateTime, requireContext()).show(parentFragmentManager, "TIMEPICKER")
        }
        binding.submitButton.setOnClickListener {
            editWeightViewModel.submitChanges(petPickerViewModel.selectionTracker.currentSelection.value!!.item.petId)
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
        _binding = null
    }
}