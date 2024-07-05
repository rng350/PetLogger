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
    val viewModel: PetMultiSelectionViewModel by viewModels({ requireParentFragment().requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFrag", "onCreateView called...")
        _binding = FragmentPetMultiSelectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupPetMultiPickerUseCase(
            petList = viewModel.selectionTracker.allOptions,
            selection = viewModel.selectionTracker.prospectiveSelection,
            selectionTracker = viewModel.selectionTracker,
            recyclerView = binding.petsList,
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext()
        )()

        binding.submitButton.setOnClickListener {
            viewModel.confirmSelection()
            requireDialog().dismiss()
        }

        binding.cancelButton.setOnClickListener {
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
        if (viewModel.currentSelectionChanged) {
            viewModel.onCurrentSelectionChanged()
        } else {
            viewModel.cancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}