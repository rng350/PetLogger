package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNewNoteBinding
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.photodisplay.stateless.GetAllCheckablePetsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllWeightsWithPetNamesUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.selectiontracker.MultiSelectionTracker

class NewNoteFragment : Fragment() {
    private var _binding: FragmentNewNoteBinding? = null
    val binding get() = _binding!!
    private lateinit var newNoteViewModel: NewNoteViewModel
    private lateinit var petMultiSelectionViewModel: PetMultiSelectionViewModel
    private lateinit var eventMultiSelectionViewModel: EventMultiSelectionViewModel
    private lateinit var weightMultiSelectionViewModel: WeightMultiSelectionViewModel
    private lateinit var mediaSelectionViewModel: MediaSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(database, mediaRepository)
        val eventRepository = EventRepository(database, mediaRepository)
        val weightRepository = WeightRepository(database)

        setAppBarTitle(getString(R.string.new_note_header))

        newNoteViewModel = ViewModelProvider(this, NewNoteViewModel.provideFactory(noteRepository)).get(NewNoteViewModel::class.java)

        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        petMultiSelectionViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(getAllPetsUseCase)).get(PetMultiSelectionViewModel::class.java)
        eventMultiSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository)).get(EventMultiSelectionViewModel::class.java)

        val getAllWeights = GetAllWeightsWithPetNamesUseCase(weightRepository)
        weightMultiSelectionViewModel = ViewModelProvider(this, WeightMultiSelectionViewModel.provideFactory(getAllWeights)).get(WeightMultiSelectionViewModel::class.java)
        mediaSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(mediaRepository = mediaRepository, maxItems = 10)).get(MediaSelectionViewModel::class.java)

        binding.newNoteViewModel = newNoteViewModel
        binding.petMultiSelectionViewModel = petMultiSelectionViewModel
        binding.eventMultiSelectionViewModel = eventMultiSelectionViewModel
        binding.weightMultiSelectionViewModel = weightMultiSelectionViewModel
        binding.mediaSelectionViewModel = mediaSelectionViewModel


        binding.lifecycleOwner = viewLifecycleOwner

        binding.submitButton.setOnClickListener {
            newNoteViewModel.submitNote(
                pets = petMultiSelectionViewModel.getPetsToAdd(),
                events = eventMultiSelectionViewModel.getEventsToAdd(),
                weights = weightMultiSelectionViewModel.getWeightsToAdd(),
                photos = mediaSelectionViewModel.getPhotosToAdd()
            )
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