package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentWeightMultiSelectionDisplayBinding
import com.hfad.petlogger.recyclerviews.SetupWeightMultiPickerSelectionDisplayUseCase

class WeightMultiSelectionDisplayFragment : Fragment() {
    var _binding: FragmentWeightMultiSelectionDisplayBinding? = null
    val binding get() = _binding!!
    private val viewModel: WeightMultiSelectionViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWeightMultiSelectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        SetupWeightMultiPickerSelectionDisplayUseCase(
            viewModel.currentSelection,
            viewModel.selectionTracker,
            binding.weightsList,
            viewLifecycleOwner
        )()

        binding.addWeightsButton.setOnClickListener {
            WeightMultiSelectionDialogFragment().show(childFragmentManager, "WEIGHT_DIALOG_FRAGMENT")
        }

        binding.resetButton.setOnClickListener {
            viewModel.resetSelection()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}