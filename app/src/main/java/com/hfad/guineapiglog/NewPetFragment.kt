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
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.hfad.guineapiglog.databinding.FragmentNewPetBinding
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId


class NewPetFragment : Fragment() {
    private var _binding: FragmentNewPetBinding? = null
    private val binding get() = _binding!!

    private val pickImage = 100
    private var imageUri: Uri? = null
    lateinit var petPhoto: ImageView

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

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        petPhoto = binding.petPhoto
        binding.petPhoto.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select pet's date of birth")
                        .build()
        binding.inputDOBButton.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
        datePicker.addOnPositiveButtonClickListener {
            viewModel.petDOB = OffsetDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }


        binding.back.setOnClickListener {
            this.findNavController().navigate(R.id.action_newPetFragment_to_homeFragment)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            petPhoto.setImageURI(imageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}