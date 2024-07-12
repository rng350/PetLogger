package com.hfad.petlogger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentNoteMultiSelectionDialogBinding
import com.hfad.petlogger.recyclerviews.SetupNoteMultiPickerUseCase

class NoteMultiSelectionDialogFragment : DialogFragment() {
    private var _binding: FragmentNoteMultiSelectionDialogBinding? = null
    val binding: FragmentNoteMultiSelectionDialogBinding get() = _binding!!
    val noteMultiSelectionViewModel: NoteMultiSelectionViewModel by viewModels({requireParentFragment().requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteMultiSelectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        binding.submitButton.setOnClickListener {
            noteMultiSelectionViewModel.confirmSelection()
            requireDialog().dismiss()
        }

        binding.cancelButton.setOnClickListener {
            requireDialog().cancel()
        }

        SetupNoteMultiPickerUseCase(
            noteList = noteMultiSelectionViewModel.selectionTracker.allOptions,
            selection = noteMultiSelectionViewModel.selectionTracker.prospectiveSelection,
            selectionTracker = noteMultiSelectionViewModel.selectionTracker,
            recyclerView = binding.notesList,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

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
        if (noteMultiSelectionViewModel.currentSelectionChanged) {
            noteMultiSelectionViewModel.onCurrentSelectionChanged()
        } else {
            noteMultiSelectionViewModel.cancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.notesList.adapter = null
        _binding = null
    }
}