package com.hfad.petlogger.screens.event.eventmultiselection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentEventMultiSelectionDialogBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupEventMultiPickerUseCase

class EventMultiSelectionDialogFragment : DialogFragment() {
    private var _binding: FragmentEventMultiSelectionDialogBinding? = null
    val binding get() = _binding!!
    val viewModel: EventMultiSelectionViewModel by viewModels({requireParentFragment().requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventMultiSelectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        SetupEventMultiPickerUseCase(
            viewModel.selectionTracker.visibleOptions,
            viewModel.selectionTracker.prospectiveSelection,
            viewModel.selectionTracker,
            binding.eventsList,
            viewLifecycleOwner
        )()

        binding.okayButton.setOnClickListener {
            viewModel.confirmSelection()
            requireDialog().dismiss()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancel()
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
        binding.eventsList.adapter = null
        _binding = null
    }
}