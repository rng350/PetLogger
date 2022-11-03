package com.hfad.guineapiglog

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import com.hfad.guineapiglog.databinding.FragmentNewEventBinding
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class NewEventFragment : Fragment() {
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!
    private var _galleryPicker: GalleryPicker? = null
    private val galleryPicker get() = _galleryPicker!!

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
        val galleryViewModelFactory = GalleryViewModelFactory(entityLinker = PhotoToEventLinker(photoDao), choiceLimit = 10, photoDao = photoDao)
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)

        val petSelectorDialog by lazy { PetMultiSelectorDialogFragment(newEventViewModel) }
        val datePicker = DatePicker.generate(newEventViewModel.eventDateTime)
        val timePicker = TimePicker.generate(newEventViewModel.eventDateTime, requireContext())

        _galleryPicker = GalleryPicker(this, binding.galleryPicker, galleryViewModel, newEventViewModel.eventID)
        galleryPicker.onCreate(savedInstanceState)

        binding.viewModel = newEventViewModel
        binding.galleryViewModel = galleryViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.inputEventDateButton.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.inputEventTimeButton.setOnClickListener {
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        binding.inputAddPetsButton.setOnClickListener {
            //multiChoiceList(view, viewModel.pets)
            petSelectorDialog.show(childFragmentManager, null)
        }

        binding.submitEventButton.setOnClickListener {
            newEventViewModel.addEvent()
            galleryPicker.saveToLocalStorage()
        }

        binding.backButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_newEventFragment_to_homeFragment)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        galleryPicker.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        galleryPicker.onDestroy()
    }
}