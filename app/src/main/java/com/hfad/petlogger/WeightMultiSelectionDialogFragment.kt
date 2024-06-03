package com.hfad.petlogger

import android.os.Bundle
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
            viewModel.allWeights,
            viewModel.currentSelection,
            viewModel.selectionTracker,
            binding.weightsList,
            viewLifecycleOwner
        )()

        binding.okayButton.setOnClickListener {
            requireDialog().dismiss()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.resetSelection()
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