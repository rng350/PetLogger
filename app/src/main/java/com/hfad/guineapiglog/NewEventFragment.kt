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
        val viewModelFactory = NewEventViewModelFactory(eventDao, eventPetDao, petDao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(NewEventViewModel::class.java)
        val petSelectorDialog by lazy { PetMultiSelectorDialogFragment(viewModel) }

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date of event")
            .build()
        binding.inputEventDateButton.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
        datePicker.addOnPositiveButtonClickListener {
            var newOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            newOffsetDateTime = newOffsetDateTime.plusHours(viewModel.eventDateTime.hour - newOffsetDateTime.hour.toLong())
            newOffsetDateTime = newOffsetDateTime.plusMinutes(viewModel.eventDateTime.minute - newOffsetDateTime.minute.toLong())
            viewModel.eventDateTime = newOffsetDateTime

            viewModel.eventDateDisplay = viewModel.eventDateTime.toString()
            Log.i("TIME", "added date ${viewModel.eventDateDisplay}")
        }

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setTitleText("Select time of event")
            .setInputMode(INPUT_MODE_KEYBOARD)
            .build()
        binding.inputEventTimeButton.setOnClickListener {
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        timePicker.addOnPositiveButtonClickListener {
            var newOffsetDateTime = viewModel.eventDateTime.plusHours(timePicker.hour.toLong() - viewModel.eventDateTime.hour)
            newOffsetDateTime = newOffsetDateTime.plusMinutes(timePicker.minute.toLong() - viewModel.eventDateTime.minute)
            viewModel.eventDateTime = newOffsetDateTime

            viewModel.eventTimeDisplay = "${timePicker.hour}:${timePicker.minute}"
            binding.eventTime.text = "${timePicker.hour}:${timePicker.minute}"
            Log.i("TIME", "added time, val ${viewModel.eventTimeDisplay}")
        }

        binding.inputAddPetsButton.setOnClickListener {
            //multiChoiceList(view, viewModel.pets)
            petSelectorDialog.show(childFragmentManager, null)
        }

        binding.submitEventButton.setOnClickListener {
            viewModel.addEvent()
        }

        binding.backButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_newEventFragment_to_homeFragment)
        }

        return view
    }
}