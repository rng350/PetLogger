package com.hfad.petlogger.screens.sections.associatedentities

import RecyclerViewPaginator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentNoteListBinding
import com.hfad.petlogger.screens.note.NoteListViewModel
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedNotesDisplayUseCase

class AssociatedNotesDisplayFragment : Fragment() {
    private var _binding: FragmentNoteListBinding? = null
    val binding: FragmentNoteListBinding get() = _binding!!

    val noteListViewModel: NoteListViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = noteListViewModel
        binding.noteListTopAppBarLayout.visibility = View.GONE

        SetupAssociatedNotesDisplayUseCase(
            notes = noteListViewModel.notes,
            noteNavigator = noteListViewModel.noteNavigator,
            recyclerView = binding.notesList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

        RecyclerViewPaginator(
            recyclerView = binding.notesList,
            isLoading = {noteListViewModel.isLoading()},
            loadMore = {noteListViewModel.load()},
            onLast = {noteListViewModel.onLastPage()}
        )

        binding.addNoteButton.setOnClickListener {
            noteListViewModel.newNoteNavigator.navigateToNewEntityScreen()
        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                noteListViewModel.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                noteListViewModel.onQueryTextChanged(newText)
                return true
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.notesList.adapter = null
        _binding = null
    }
}