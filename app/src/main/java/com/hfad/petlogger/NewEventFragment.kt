package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNewEventBinding
import com.hfad.petlogger.entitylinkers.PhotoToEventLinker
import com.hfad.petlogger.photoselection.GalleryPicker
import com.hfad.petlogger.photoselection.GalleryViewModel
import com.hfad.petlogger.photoselection.GalleryViewModelFactory
import kotlinx.coroutines.*

class NewEventFragment : Fragment() {
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!
    private var _galleryPicker: GalleryPicker? = null
    private val galleryPicker get() = _galleryPicker!!
    var petSelector: PetMultiSelectorDialogFragment<NewEventViewModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewEventBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val eventDao = PetLoggerDatabase.getInstance(application).eventDao
        val eventPetDao = PetLoggerDatabase.getInstance(application).eventPetDao
        val photoDao = PetLoggerDatabase.getInstance(application).photoDao

        val newEventViewModelFactory = NewEventViewModelFactory(eventDao, eventPetDao, petDao)
        val newEventViewModel = ViewModelProvider(this, newEventViewModelFactory).get(NewEventViewModel::class.java)
        val galleryViewModelFactory = GalleryViewModelFactory(
            entityLinker = PhotoToEventLinker(photoDao),
            photoDao = photoDao,
            photosSelected = newEventViewModel.eventPhotoSelection)
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)

        //val petSelectorDialog by lazy { PetMultiSelectorDialogFragment(newEventViewModel) }
        //val datePicker = DatePicker.generate(newEventViewModel.eventDateTime)
        //val timePicker = TimePicker.generate(newEventViewModel.eventDateTime, requireContext())

        _galleryPicker = GalleryPicker(this, binding.galleryPicker, galleryViewModel, newEventViewModel.eventID)
        galleryPicker.onCreate(savedInstanceState)

        binding.viewModel = newEventViewModel
        binding.galleryViewModel = galleryViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val mainActivity = (activity as MainActivity)
        mainActivity.setTopAppBarTitle(getString(R.string.new_event_header))
        mainActivity.disableTopAppBarSubtitle()

        binding.inputEventDateButton.setOnClickListener {
            DatePicker.generate(newEventViewModel.eventDateTime).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.inputEventTimeButton.setOnClickListener {
            TimePicker.generate(newEventViewModel.eventDateTime, requireContext()).show(parentFragmentManager, "TIME_PICKER")
        }

        binding.inputAddPetsButton.setOnClickListener {
            //multiChoiceList(view, viewModel.pets)
            //PetMultiSelectorDialogFragment.newInstance(newEventViewModel).show(childFragmentManager, "PET_SELECTOR")

            binding.inputAddPetsButton.isEnabled = false

            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                petSelector = PetMultiSelectorDialogFragment.newInstance(newEventViewModel)
                petSelector!!.show(childFragmentManager, "PET_SELECTOR")
                delay(200)
                binding.inputAddPetsButton.isEnabled = true
            }
        }

        binding.submitEventButton.setOnClickListener {
            newEventViewModel.addEvent()
            galleryPicker.saveToLocalStorage()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        galleryPicker.onResume()
    }

    override fun onStop() {
        super.onStop()
        binding.inputAddPetsButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        galleryPicker.onDestroy()
    }
}