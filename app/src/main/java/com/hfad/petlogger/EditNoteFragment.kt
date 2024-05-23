package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditEventBinding
import com.hfad.petlogger.databinding.FragmentEditNoteBinding
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    val binding get() = _binding!!

    private lateinit var viewModel: EditNoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNoteBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        val noteId = EditNoteFragmentArgs.fromBundle(requireArguments()).noteId

        val photoDao = PetLoggerDatabase.getInstance(requireContext()).photoDao
        val mediaRepository = MediaRepository(photoDao, requireContext())
        val noteDao = PetLoggerDatabase.getInstance(requireContext()).noteDao
        val noteRepository = NoteRepository(noteDao, mediaRepository)
        viewModel = ViewModelProvider(this, EditNoteViewModel.provideFactory(noteRepository, noteId)).get(EditNoteViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.submitButton.setOnClickListener {
            viewModel.submitChanges()
        }

        binding.resetButton.setOnClickListener {
            viewModel.reset()
        }

        viewModel.goBack.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val action = EditNoteFragmentDirections.actionEditNoteFragmentToViewNoteFragment(noteId)
                findNavController().navigate(action)
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}