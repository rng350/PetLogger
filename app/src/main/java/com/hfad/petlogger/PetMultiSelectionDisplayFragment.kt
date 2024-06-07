package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetMultiSelectionDisplayBinding
import com.hfad.petlogger.recyclerviews.SetupPetMultiPickerSelectionDisplayUseCase

class PetMultiSelectionDisplayFragment : Fragment() {
    private var _binding: FragmentPetMultiSelectionDisplayBinding? = null
    val binding: FragmentPetMultiSelectionDisplayBinding
        get() = _binding!!

    val viewModel: PetMultiSelectionViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetMultiSelectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.logSomething("PetMultiSelDisplay", "Message from Pet Selection Display Fragment... VM")

        binding.addPetsButton.setOnClickListener {
            PetMultiSelectionDialogFragment().show(childFragmentManager, "PET_MULTI_PICKER")
        }

        binding.resetButton.setOnClickListener {
            viewModel.resetSelection()
        }

        SetupPetMultiPickerSelectionDisplayUseCase(
            viewModel.currentSelection,
            viewModel.selectionTracker,
            binding.petsList,
            viewLifecycleOwner,
            requireContext()
        )()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}