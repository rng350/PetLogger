package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetMultiSelectionDialogBinding
import com.hfad.petlogger.recyclerviews.SetupPetMultiPickerUseCase

class PetMultiSelectionDialogFragment : DialogFragment() {
    private var _binding: FragmentPetMultiSelectionDialogBinding? = null
    val binding: FragmentPetMultiSelectionDialogBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFrag", "onCreateView called...")
        _binding = FragmentPetMultiSelectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        val viewModel: PetMultiSelectionViewModel by viewModels({ requireParentFragment().requireParentFragment() })

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.logSomething("PetMultiSelDialog", "Message from PetMultiSelectionDialogFragment... VM")

        SetupPetMultiPickerUseCase(
            viewModel.allPets,
            viewModel.currentSelection,
            viewModel.selectionTracker,
            binding.petsList,
            viewLifecycleOwner,
            requireContext()
        )()

        binding.cancelButton.setOnClickListener {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}