package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.get
import com.hfad.petlogger.databinding.FragmentEditWeightBinding
import com.hfad.petlogger.entities.Pet

class EditWeightFragment : Fragment() {
    private var _binding: FragmentEditWeightBinding? = null
    val binding get() = _binding!!
    private var petPickerDialog: PetSinglePickerDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditWeightBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application

        val weightID = EditWeightFragmentArgs.fromBundle(requireArguments()).weightId
        val weightDao = PetLoggerDatabase.getInstance(application).weightDao
        val petDao = PetLoggerDatabase.getInstance(application).petDao

        val editWeightViewModelFactory = EditWeightViewModelFactory(weightID, weightDao)
        val editWeightViewModel = ViewModelProvider(this, editWeightViewModelFactory).get(EditWeightViewModel::class.java)
        binding.viewModel = editWeightViewModel

        childFragmentManager.setFragmentResultListener(PetSinglePickerDialogFragment.requestKey, viewLifecycleOwner) { _, bundle ->
            val selectedPet = bundle.get(PetSinglePickerDialogFragment.resultBundleKey) as Pet
            editWeightViewModel.pet.value = selectedPet
            Log.d("EditWeightFrag", "Pet selected! ${selectedPet.toString()}")
        }

        val mainActivity = (activity as MainActivity)
        mainActivity.setTopAppBarTitle(getString(R.string.edit_weight_header))
        mainActivity.disableTopAppBarSubtitle()

        binding.changeAssocPetButton.setOnClickListener{
            petPickerDialog = PetSinglePickerDialogFragment.newInstance(editWeightViewModel.pet.value)
            petPickerDialog?.show(childFragmentManager, "PET_SINGLE_PICKER")
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}