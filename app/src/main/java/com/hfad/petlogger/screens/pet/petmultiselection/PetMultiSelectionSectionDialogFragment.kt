package com.hfad.petlogger.screens.pet.petmultiselection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetMultiSelectionSectionDialogBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupPetMultiPickerUseCase

class PetMultiSelectionSectionDialogFragment : DialogFragment() {
    private var _binding: FragmentPetMultiSelectionSectionDialogBinding? = null
    val binding: FragmentPetMultiSelectionSectionDialogBinding
        get() = _binding!!
    val viewModel: PetMultiSelectionViewModel by viewModels({ requireParentFragment().requireParentFragment().requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFrag", "onCreateView called...")
        _binding = FragmentPetMultiSelectionSectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupPetMultiPickerUseCase(
            petList = viewModel.selectionTracker.visibleOptions,
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
            viewModel.cancel()
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