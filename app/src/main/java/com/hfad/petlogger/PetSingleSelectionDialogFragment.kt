package com.hfad.petlogger

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetSingleSelectionDialogBinding
import com.hfad.petlogger.recyclerviews.SetupPetPickerUseCase

class PetSingleSelectionDialogFragment : DialogFragment() {
    private var _binding: FragmentPetSingleSelectionDialogBinding? = null
    val binding: FragmentPetSingleSelectionDialogBinding get() = _binding!!
    val petSingleSelectionViewModel: PetSingleSelectionViewModel by viewModels ({requireParentFragment().requireParentFragment().requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetSingleSelectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.viewModel = petSingleSelectionViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupPetPickerUseCase(
            petSingleSelectionViewModel.selectionTracker.allOptions,
            petSingleSelectionViewModel.selectionTracker.prospectiveSelection,
            petSingleSelectionViewModel.selectionTracker,
            binding.petsList,
            viewLifecycleOwner,
            requireContext())()

        binding.submitButton.setOnClickListener{
            petSingleSelectionViewModel.confirmSelection()
            requireDialog().dismiss()
        }

        binding.cancelButton.setOnClickListener{
            petSingleSelectionViewModel.cancelSelection()
            requireDialog().cancel()
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

    override fun onStop() {
        super.onStop()
        Log.d("PetSingleDialog", "onStop called")
        if (petSingleSelectionViewModel.currentSelectionChanged) {
            petSingleSelectionViewModel.onCurrentSelectionChanged()
        } else {
            petSingleSelectionViewModel.cancelSelection()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}