package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNoteListBinding
import com.hfad.petlogger.photodisplay.stateful.GetAllNotesForDisplayUseCase
import com.hfad.petlogger.recyclerviews.SetupShortenedNotesListDisplayUseCase
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository

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

        setAppBarTitle(getString(R.string.note_list_header))

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, requireContext())
        val noteRepository = NoteRepository(PetLoggerDatabase.getInstance(requireContext()), mediaRepository)

        binding.lifecycleOwner = viewLifecycleOwner

        val getAllNotes = GetAllNotesForDisplayUseCase(noteRepository)
        viewModel = ViewModelProvider(this, NoteListViewModel.provideFactory(getAllNotes)).get(NoteListViewModel::class.java)
        binding.viewModel = viewModel

        SetupShortenedNotesListDisplayUseCase(
            notes = viewModel.notes,
            noteNavigator = viewModel.noteNavigator,
            recyclerView = binding.notesList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

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

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.notesList.adapter = null
        _binding = null
    }
}