package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNoteListBinding
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator
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

        val noteDao = PetLoggerDatabase.getInstance(requireContext()).noteDao
        val photoDao = PetLoggerDatabase.getInstance(requireContext()).photoDao

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, requireContext())
        val noteRepository = NoteRepository(PetLoggerDatabase.getInstance(requireContext()), mediaRepository)

        binding.lifecycleOwner = viewLifecycleOwner
        val viewModelFactory = NoteListViewModel.provideFactory(noteRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(NoteListViewModel::class.java)
        binding.viewModel = viewModel

        BindingInterfaceCreator.setupNoteListItemAdapter(
            viewModel.notes,
            binding.notesList,
            viewLifecycleOwner,
            viewModel.noteNavigator)

        viewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                val action = NoteListFragmentDirections.actionNoteListFragmentToViewNoteFragment(it)
                this.findNavController().navigate(action)
                viewModel.noteNavigator.onNavigated()
            }
        }

        binding.addNoteButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_noteListFragment_to_newNoteFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}