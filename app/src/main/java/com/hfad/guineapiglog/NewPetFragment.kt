package com.hfad.guineapiglog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.hfad.guineapiglog.databinding.FragmentNewPetBinding
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId


class NewPetFragment : Fragment() {
    private var _binding: FragmentNewPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val viewModelFactory = NewPetViewModelFactory(petDao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(NewPetViewModel::class.java)
        //val datePicker = DatePicker.generate(viewModel.petDOB)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val getPicture = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            // keeps app from crashing from read permission revoking on OS reboot
            context?.contentResolver?.takePersistableUriPermission(it!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            binding.petPhoto.setImageURI(it)
            binding.viewModel?.petProfilePicURI = it
        }

        binding.petPhoto.setOnClickListener {
            getPicture.launch(arrayOf("image/*"))
        }

        binding.inputDOBButton.setOnClickListener {
            //datePicker.show(parentFragmentManager, "DATE_PICKER")
            DatePicker.generate(viewModel.petDOB).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.back.setOnClickListener {
            this.findNavController().navigate(R.id.action_newPetFragment_to_homeFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}