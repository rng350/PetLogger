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
import com.hfad.petlogger.repositories.PetRepository

class NewNoteFragment : Fragment() {
    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var newNoteViewModel: NewNoteViewModel
    lateinit var petMultiSelectionViewModel: PetMultiSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val photoDao = database.photoDao
        val petDao = database.petDao
        val mediaRepository = MediaRepository(photoDao, requireContext())
        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(petDao, mediaRepository)

        newNoteViewModel = ViewModelProvider(this, NewNoteViewModel.provideFactory(noteRepository)).get(NewNoteViewModel::class.java)
        petMultiSelectionViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(petRepository)).get(PetMultiSelectionViewModel::class.java)
        petMultiSelectionViewModel.logSomething("NewNoteFr", "message from NewNoteFragment... VM")

        binding.newNoteViewModel = newNoteViewModel
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.submitButton.setOnClickListener {
            newNoteViewModel.submitNote(pets = petMultiSelectionViewModel.getPetsToAdd())
        }

        binding.clearButton.setOnClickListener {
            newNoteViewModel.clear()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        newNoteViewModel.goBack.observe(viewLifecycleOwner) {
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