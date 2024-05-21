package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNewNoteBinding
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository

class NewNoteFragment : Fragment() {
    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NewNoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val noteDao = PetLoggerDatabase.getInstance(application).noteDao
        val photoDao = PetLoggerDatabase.getInstance(application).photoDao
        val mediaRepository = MediaRepository(photoDao, requireContext())
        val noteRepository = NoteRepository(noteDao, mediaRepository)
        val viewModelFactory = NewNoteViewModel.provideFactory(noteRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(NewNoteViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.submitButton.setOnClickListener {
            viewModel.submitNote()
        }

        binding.clearButton.setOnClickListener {
            viewModel.clear()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.goBack.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigate(NewNoteFragmentDirections.actionNewNoteFragmentToNoteListFragment())
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}