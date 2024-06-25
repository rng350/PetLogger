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
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetSinglePickerDialogBinding
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.recyclerviews.SetupPetPickerUseCase
import java.lang.ClassCastException

class PetSinglePickerDialogFragment : DialogFragment() {
    private var _binding: FragmentPetSinglePickerDialogBinding? = null
    val binding: FragmentPetSinglePickerDialogBinding
        get() = _binding!!
    companion object {
        val requestKey by lazy {"PetSinglePickerDialog"}
        val resultBundleKey by lazy {"petSinglePickerBundle"}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFrag", "onCreateView called...")
        _binding = FragmentPetSinglePickerDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        val viewModel: PetSinglePickerDialogViewModel by viewModels ({requireParentFragment()})

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
                setFragmentResult(requestKey, bundleOf(PetSinglePickerDialogFragment.resultBundleKey to it))
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
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}