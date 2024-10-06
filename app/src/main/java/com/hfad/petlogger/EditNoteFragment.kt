package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditNoteBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllWeightsWithPetNamesUseCase
import com.hfad.petlogger.photodisplay.stateless.GetEventsOfNoteUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPetsOfNoteUseCase
import com.hfad.petlogger.photodisplay.stateless.GetPhotosOfNoteUseCase
import com.hfad.petlogger.photodisplay.stateless.GetWeightsOfNoteUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    val binding get() = _binding!!

    private lateinit var editNoteViewModel: EditNoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNoteBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val noteId = EditNoteFragmentArgs.fromBundle(requireArguments()).noteId

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val noteRepository = NoteRepository(PetLoggerDatabase.getInstance(requireContext()), mediaRepository)
        editNoteViewModel = ViewModelProvider(this, EditNoteViewModel.provideFactory(noteRepository, noteId)).get(EditNoteViewModel::class.java)
        binding.editNoteViewModel = editNoteViewModel

        val eventRepository = EventRepository(database, mediaRepository)
        val getEventsOfNoteUseCase = GetEventsOfNoteUseCase(noteRepository, noteId)
        val eventMultiSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository, getEventsOfNoteUseCase)).get(EventMultiSelectionViewModel::class.java)
        binding.eventMultiSelectionViewModel = eventMultiSelectionViewModel

        val petRepository = PetRepository(database, mediaRepository)
        val getAllPets = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val getPetsOfNote = GetPetsOfNoteUseCase(noteRepository, noteId)
        //val getPetsOfNote = null
        val petMultiSelectionViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(getAllPets, getPetsOfNote)).get(PetMultiSelectionViewModel::class.java)
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel

        val getPhotosOfNote = GetPhotosOfNoteUseCase(noteRepository, noteId)
        val mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(mediaRepository, getPhotosOfNote, maxItems = 10)).get(MediaSelectionViewModel::class.java)
        binding.mediaSelectionViewModel = mediaSelectionViewModel

        val weightRepository = WeightRepository(database)
        val getAllWeights = GetAllWeightsWithPetNamesUseCase(weightRepository)
        val getWeightsOfNote = GetWeightsOfNoteUseCase(noteRepository, noteId)
        val weightMultiSelectionViewModel = ViewModelProvider(this, WeightMultiSelectionViewModel.provideFactory(getAllWeights, getWeightsOfNote)).get(WeightMultiSelectionViewModel::class.java)
        binding.weightMultiSelectionViewModel = weightMultiSelectionViewModel


        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.submitButton.setOnClickListener {
            editNoteViewModel.submitChanges(
                eventsToAdd = eventMultiSelectionViewModel.getEventsToAdd(),
                eventsToRemove = eventMultiSelectionViewModel.getEventsToRemove(),
                petsToAdd = petMultiSelectionViewModel.getPetsToAdd(),
                petsToRemove = petMultiSelectionViewModel.getPetsToRemove(),
                photosToAdd = mediaSelectionViewModel.getPhotosToAdd(),
                photosToRemove = mediaSelectionViewModel.getPhotosToRemove()
            )
        }

        val confirmAction = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_note_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_note_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editNoteViewModel.delete()
            },
            context = requireContext()
        )
        binding.deleteButton.setOnClickListener {
            confirmAction()
        }

        editNoteViewModel.fetchedNote.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(title = it.title.ifEmpty { getString(R.string.view_untitled_note_header) }, subtitle = getString(R.string.editing_note_details))
            }
        })

        editNoteViewModel.goBack.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val action = EditNoteFragmentDirections.actionEditNoteFragmentToViewNoteFragment(noteId)
                findNavController().navigateSafe(action)
            }
        })

        editNoteViewModel.goToNotesList.observe(viewLifecycleOwner) {
            if (it == true) {
                editNoteViewModel.onNavigateToNotesList()
                findNavController().navigateSafe(EditNoteFragmentDirections.actionEditNoteFragmentToNoteListFragment())
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}