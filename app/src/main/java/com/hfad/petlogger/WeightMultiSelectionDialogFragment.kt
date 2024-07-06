package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentWeightMultiSelectionDialogBinding
import com.hfad.petlogger.recyclerviews.SetupWeightMultiPickerUseCase

class WeightMultiSelectionDialogFragment : DialogFragment() {
    private var _binding: FragmentWeightMultiSelectionDialogBinding? = null
    val binding get() = _binding!!

    private val viewModel: WeightMultiSelectionViewModel by viewModels({requireParentFragment().requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWeightMultiSelectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        SetupWeightMultiPickerUseCase(
            viewModel.selectionTracker.allOptions,
            viewModel.selectionTracker.prospectiveSelection,
            viewModel.selectionTracker,
            binding.weightsList,
            viewLifecycleOwner
        )()

        binding.okayButton.setOnClickListener {
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
        if (viewModel.currentSelectionChanged) {
            viewModel.onCurrentSelectionChanged()
        } else {
            viewModel.cancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.weightsList.adapter = null
        _binding = null
    }
}