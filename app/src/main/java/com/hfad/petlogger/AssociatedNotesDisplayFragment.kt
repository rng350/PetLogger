package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentAssociatedNotesDisplayBinding
import com.hfad.petlogger.recyclerviews.SetupAssociatedNotesDisplayUseCase

class AssociatedNotesDisplayFragment : Fragment() {
    private var _binding: FragmentAssociatedNotesDisplayBinding? = null
    val binding: FragmentAssociatedNotesDisplayBinding get() = _binding!!

    val associatedNotesDisplayViewModel: AssociatedNotesDisplayViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedNotesDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.associatedNotesDisplayViewModel = associatedNotesDisplayViewModel

        SetupAssociatedNotesDisplayUseCase(
            notes = associatedNotesDisplayViewModel.events,
            noteNavigator = associatedNotesDisplayViewModel.noteNavigator,
            recyclerView = binding.notesList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.notesList.adapter = null
        _binding = null
    }
}