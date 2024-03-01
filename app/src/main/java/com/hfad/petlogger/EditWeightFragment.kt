package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.get
import com.hfad.petlogger.databinding.FragmentEditWeightBinding

class EditWeightFragment : Fragment(), PetSinglePickerDialogFragment.PetSinglePickerDialogListener {

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

        val mainActivity = (activity as MainActivity)
        mainActivity.setTopAppBarTitle(getString(R.string.edit_weight_header))
        mainActivity.disableTopAppBarSubtitle()

        binding.changeAssocPetButton.setOnClickListener{
            petPickerDialog = PetSinglePickerDialogFragment.newInstance(editWeightViewModel.pet.value, this)
            petPickerDialog?.show(childFragmentManager, "PET_SINGLE_PICKER")

            /*editWeightViewModel.pet.value?.let {
                petPickerDialog = PetSinglePickerDialogFragment.newInstance(it)
                petPickerDialog?.show(parentFragmentManager, "PET_SINGLE_PICKER")
            }*/
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPetSingleSelectionConfirmation() {
        petPickerDialog?.let {
            it.binding.viewModel?.selectedPet?.value?.item?.let { selected ->
                binding.viewModel?.pet?.value = selected.pet
            }
        }
    }
}