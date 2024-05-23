package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentViewNoteBinding
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository

class ViewNoteFragment : Fragment() {
    private var _binding: FragmentViewNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewNoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewNoteBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        val photoDao = PetLoggerDatabase.getInstance(requireContext()).photoDao
        val mediaRepository = MediaRepository(photoDao, requireContext())
        val noteDao = PetLoggerDatabase.getInstance(requireContext()).noteDao
        val noteRepository = NoteRepository(noteDao, mediaRepository)
        val noteId = ViewNoteFragmentArgs.fromBundle(requireArguments()).noteId

        viewModel = ViewModelProvider(this, ViewNoteViewModel.provideFactory(noteRepository, noteId)).get(ViewNoteViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.note.observe(viewLifecycleOwner, Observer {
            it?.let {
                val mainActivity = (activity as MainActivity)
                mainActivity.setTopAppBarTitle(it.title)
                mainActivity.setTopAppBarSubtitle(getString(R.string.viewing_note_details))
            }
        })

        binding.editButton.setOnClickListener {
            // TODO: Implement after Edit Note Screen implemented
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}