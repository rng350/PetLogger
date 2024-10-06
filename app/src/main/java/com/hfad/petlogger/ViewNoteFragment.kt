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
import com.hfad.petlogger.photodisplay.stateful.GetEventsOfNoteForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateful.GetPetsOfNoteForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateful.GetPhotosOfNoteForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreEventsOfNoteUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMorePetsOfNoteUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMorePhotosOfNoteUseCase
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository

class ViewNoteFragment : Fragment() {
    private var _binding: FragmentViewNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewNoteViewModel: ViewNoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewNoteBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val noteRepository = NoteRepository(database, mediaRepository)
        val noteId = ViewNoteFragmentArgs.fromBundle(requireArguments()).noteId

        viewNoteViewModel = ViewModelProvider(this, ViewNoteViewModel.provideFactory(noteRepository, noteId)).get(ViewNoteViewModel::class.java)
        binding.viewNoteViewModel = viewNoteViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val getPhotosOfNote = GetMorePhotosOfNoteUseCase(noteRepository, noteId, photosAmt = 9)
        val associatedPhotosViewModel = ViewModelProvider(this, AssociatedPhotosDisplayViewModel.provideFactory(getPhotosOfNote)).get(AssociatedPhotosDisplayViewModel::class.java)
        binding.associatedPhotosDisplayViewModel = associatedPhotosViewModel

        val getAssociatedPets = GetMorePetsOfNoteUseCase(noteRepository, noteId, petsAmt = 10)
        val associatedPetsDisplayViewModel = ViewModelProvider(this, AssociatedPetsDisplayViewModel.provideFactory(getAssociatedPets)).get(AssociatedPetsDisplayViewModel::class.java)
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        val getEventsOfNote = GetMoreEventsOfNoteUseCase(noteRepository, noteId, eventAmt = 10)
        val associatedEventsDisplayViewModel = ViewModelProvider(this, AssociatedEventsDisplayViewModel.provideFactory(getEventsOfNote)).get(AssociatedEventsDisplayViewModel::class.java)
        binding.associatedEventsDisplayViewModel = associatedEventsDisplayViewModel

        viewNoteViewModel.note.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(title = it.title.ifEmpty { getString(R.string.view_untitled_note_header) }, subtitle = getString(R.string.viewing_note_details))
            }
        })

        binding.editButton.setOnClickListener {
            findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToEditNoteFragment(noteId))
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        associatedPetsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedPetsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToViewPetFragment(it))
            }
        }

        associatedPhotosViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedPhotosViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewNoteFragmentDirections.actionViewNoteFragmentToViewPhotoFragment(it))
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}