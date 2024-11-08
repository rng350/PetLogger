package com.hfad.petlogger.screens.event.eventmultiselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentEventMultiSelectionDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupEventMultiPickerSelectionDisplayUseCase

class EventMultiSelectionDisplayFragment : Fragment() {
    private var _binding: FragmentEventMultiSelectionDisplayBinding? = null
    val binding get() = _binding!!

    val viewModel: EventMultiSelectionViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventMultiSelectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        SetupEventMultiPickerSelectionDisplayUseCase(
            viewModel.currentSelection,
            viewModel.selectionTracker,
            binding.eventsList,
            viewLifecycleOwner
        )()

        binding.addEventsButton.setOnClickListener {
            EventMultiSelectionDialogFragment().show(childFragmentManager, "EVENT_PICKER_DIALOG_FRAGMENT")
        }

        binding.resetButton.setOnClickListener {
            viewModel.resetSelection()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.eventsList.adapter = null
        _binding = null
    }
}