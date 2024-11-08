package com.hfad.petlogger.screens.note.notemultiselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentNoteMultiSelectionDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupNoteMultiPickerSelectionDisplayUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NoteMultiSelectionDisplayFragment : Fragment() {
    private var _binding: FragmentNoteMultiSelectionDisplayBinding? = null
    val binding: FragmentNoteMultiSelectionDisplayBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteMultiSelectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val noteMultiSelectionViewModel: NoteMultiSelectionViewModel by viewModels({requireParentFragment()})
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        SetupNoteMultiPickerSelectionDisplayUseCase(
            selection = noteMultiSelectionViewModel.selectionTracker.currentSelection,
            selectionTracker = noteMultiSelectionViewModel.selectionTracker,
            recyclerView = binding.notesList,
            lifecycleOwner = viewLifecycleOwner,
        ).invoke()

        binding.addNotesButton.setOnClickListener {
            binding.addNotesButton.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                NoteMultiSelectionDialogFragment().show(childFragmentManager, "PET_MULTI_PICKER")
                delay(200)
                binding.addNotesButton.isEnabled = true
            }
        }

        binding.resetButton.setOnClickListener {
            noteMultiSelectionViewModel.reset()
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        binding.addNotesButton.isEnabled = true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.notesList.adapter = null
        _binding = null
    }
}