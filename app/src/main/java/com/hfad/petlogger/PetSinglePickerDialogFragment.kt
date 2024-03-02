package com.hfad.petlogger

import android.app.Dialog
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.hfad.petlogger.databinding.FragmentPetSinglePickerDialogBinding
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.recyclerviews.SetupPetPickerUseCase
import java.lang.ClassCastException

class PetSinglePickerDialogFragment : DialogFragment() {
    private var _binding: FragmentPetSinglePickerDialogBinding? = null
    val binding: FragmentPetSinglePickerDialogBinding
        get() = _binding!!

    private var _selectedPet: Pet? = null
    val selectedPet: Pet
        get() = _selectedPet!!
    companion object {
        val requestKey by lazy {"PetSinglePickerDialog"}
        val resultBundleKey by lazy {"petSinglePickerBundle"}
        fun newInstance(selectedPet: Pet? = null): PetSinglePickerDialogFragment {
            val instance = PetSinglePickerDialogFragment()
            instance.setupDialogFragment(selectedPet)
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFrag", "onCreateView called...")

        savedInstanceState?.let {
            _selectedPet = it.get("PET") as Pet
        }

        _binding = FragmentPetSinglePickerDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireActivity().application

        val petDao = PetLoggerDatabase.getInstance(application).petDao

        val viewModel = ViewModelProvider(this, PetSinglePickerDialogViewModelFactory(petDao, selectedPet))
            .get(PetSinglePickerDialogViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupPetPickerUseCase(
            viewModel.pets,
            viewModel.selectedPet,
            binding.petsList,
            viewLifecycleOwner,
            requireContext())()

        binding.submitButton.setOnClickListener{
            viewModel.selectedPet.value?.item?.pet?.let {
                setFragmentResult(PetSinglePickerDialogFragment.requestKey, bundleOf(PetSinglePickerDialogFragment.resultBundleKey to it))
                requireDialog().dismiss()
            }
        }

        binding.cancelButton.setOnClickListener{
            requireDialog().dismiss()
        }

        return view
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("PET", _selectedPet)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("DialogFrag", "onCreateDialog called...")
        return super.onCreateDialog(savedInstanceState)
    }

    private fun setupDialogFragment(selectedPet: Pet? = null) {
        this._selectedPet = selectedPet
    }
}