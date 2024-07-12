package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentViewEventBinding
import com.hfad.petlogger.photodisplay.stateful.GetNotesOfEventForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateful.GetPetsOfEventForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateful.GetPhotosOfEventForDisplayUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository

class ViewEventFragment : Fragment() {
    private var _binding: FragmentViewEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewEventBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val eventDao = database.eventDao
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val eventRepository = EventRepository(database, mediaRepository)

        val eventId = ViewEventFragmentArgs.fromBundle(requireArguments()).eventId
        val viewEventViewModel = ViewModelProvider(this, ViewEventViewModel.provideFactory(eventDao, eventId)).get(ViewEventViewModel::class.java)
        binding.viewEventViewModel = viewEventViewModel

        val getAssociatedPhotos = GetPhotosOfEventForDisplayUseCase(eventId, eventRepository)
        val associatedPhotosDisplayViewModel = ViewModelProvider(this, AssociatedPhotosDisplayViewModel.provideFactory(getAssociatedPhotos)).get(AssociatedPhotosDisplayViewModel::class.java)
        binding.associatedPhotosDisplayViewModel = associatedPhotosDisplayViewModel

        val getAssociatedPets = GetPetsOfEventForDisplayUseCase(eventId, eventRepository)
        val associatedPetsDisplayViewModel = ViewModelProvider(this, AssociatedPetsDisplayViewModel.provideFactory(getAssociatedPets)).get(AssociatedPetsDisplayViewModel::class.java)
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        val getNotesOfEvent = GetNotesOfEventForDisplayUseCase(eventRepository, eventId)
        val associatedNotesDisplayViewModel = ViewModelProvider(this, AssociatedNotesDisplayViewModel.provideFactory(getNotesOfEvent)).get(AssociatedNotesDisplayViewModel::class.java)

        viewEventViewModel.event.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(title = it.title, subtitle = getString(R.string.viewing_event_details))
            }
        })

        associatedPetsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedPetsDisplayViewModel.navigator.onNavigated()
                this.findNavController().navigate(ViewEventFragmentDirections.actionViewEventFragmentToViewPetFragment(it))
            }
        }

        associatedPhotosDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedPhotosDisplayViewModel.navigator.onNavigated()
                findNavController().navigate(ViewEventFragmentDirections.actionViewEventFragmentToViewPhotoFragment(it))
            }
        }

        associatedNotesDisplayViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                associatedNotesDisplayViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewEventFragmentDirections.actionViewEventFragmentToViewNoteFragment(it))
            }
        }

        binding.editEventButton.setOnClickListener {
            this.findNavController().navigate(ViewEventFragmentDirections.actionViewEventFragmentToEditEventFragment(eventId))
        }

        binding.backButton.setOnClickListener {
            this.findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}