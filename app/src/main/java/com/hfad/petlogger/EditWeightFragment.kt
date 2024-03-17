package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditWeightBinding
import com.hfad.petlogger.entities.Pet

class EditWeightFragment : Fragment() {
    private var _binding: FragmentEditWeightBinding? = null
    val binding get() = _binding!!
    lateinit var petPickerViewModel: PetSinglePickerDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditWeightBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val view = binding.root

        val application = requireNotNull(this.activity).application

        val weightID = EditWeightFragmentArgs.fromBundle(requireArguments()).weightId
        val weightDao = PetLoggerDatabase.getInstance(application).weightDao

        val editWeightViewModelFactory = EditWeightViewModelFactory(weightID, weightDao)
        val editWeightViewModel = ViewModelProvider(this, editWeightViewModelFactory).get(EditWeightViewModel::class.java)
        binding.viewModel = editWeightViewModel

        val petDao = PetLoggerDatabase.getInstance(application).petDao
        editWeightViewModel.pet.observeOnce(viewLifecycleOwner) {
            it?.let {
                petPickerViewModel = ViewModelProvider(
                    this,
                    PetSinglePickerDialogViewModel.provideFactory(petDao, it)
                )[PetSinglePickerDialogViewModel::class.java]
            }
        }

        childFragmentManager.setFragmentResultListener(PetSinglePickerDialogFragment.requestKey, viewLifecycleOwner) { _, bundle ->
            val selectedPet = bundle.get(PetSinglePickerDialogFragment.resultBundleKey) as Pet
            editWeightViewModel.pet.value = selectedPet
            Log.d("EditWeightFrag", "Pet selected! ${selectedPet.toString()}")
        }

        val mainActivity = (activity as MainActivity)
        mainActivity.setTopAppBarTitle(getString(R.string.edit_weight_header))
        mainActivity.disableTopAppBarSubtitle()

        binding.changeAssocPetButton.setOnClickListener{
            PetSinglePickerDialogFragment().show(childFragmentManager, "PET_SINGLE_PICKER")
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