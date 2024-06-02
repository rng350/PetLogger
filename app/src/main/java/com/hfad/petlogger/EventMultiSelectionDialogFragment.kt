package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentEventMultiSelectionDialogBinding
import com.hfad.petlogger.recyclerviews.SetupEventMultiPickerUseCase
import com.hfad.petlogger.repositories.EventRepository

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
            viewModel.allEvents,
            viewModel.currentSelection,
            viewModel.selectionTracker,
            binding.eventsList,
            viewLifecycleOwner
        )()

        binding.okayButton.setOnClickListener {
            requireDialog().dismiss()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.resetSelection()
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