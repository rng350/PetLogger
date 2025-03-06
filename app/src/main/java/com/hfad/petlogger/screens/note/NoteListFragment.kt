package com.hfad.petlogger.screens.note

import RecyclerViewPaginator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.databinding.FragmentNoteListBinding
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.domain.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.screens.sections.recyclerviews.SetupShortenedNotesListDisplayUseCase

class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: NoteListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val noteRepository = NoteRepository(PetLoggerDatabase.getInstance(requireContext()), mediaRepository)
        val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, noteAmt=10)
        val getMoreOfSearchedNotesFromAll = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10)

        viewModel = ViewModelProvider(this,
            NoteListViewModel.provideFactory(getAllNotes, getMoreOfSearchedNotesFromAll)
        ).get(NoteListViewModel::class.java)
        binding.viewModel = viewModel

        SetupShortenedNotesListDisplayUseCase(
            notes = viewModel.notes,
            noteNavigator = viewModel.noteNavigator,
            recyclerView = binding.notesList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

        RecyclerViewPaginator(
            recyclerView = binding.notesList,
            loadMore = {viewModel.load()},
            isLoading = {viewModel.isLoading()},
            onLast = {viewModel.onLastPage()}
        )

        if (findNavController().previousBackStackEntry == null) {
            binding.noteListTopAppBar.navigationIcon = null
        } else {
            binding.noteListTopAppBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        viewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                val action = NoteListFragmentDirections.actionNoteListFragmentToViewNoteFragment(it)
                this.findNavController().navigateSafe(action)
                viewModel.noteNavigator.onNavigated()
            }
        }

        binding.addNoteButton.setOnClickListener {
            this.findNavController().navigateSafe(NoteListFragmentDirections.actionNoteListFragmentToNewNoteFragment())
        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onQueryTextChanged(newText)
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