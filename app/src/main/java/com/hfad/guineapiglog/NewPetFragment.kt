package com.hfad.guineapiglog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.hfad.guineapiglog.databinding.FragmentNewPetBinding
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId


class NewPetFragment : Fragment() {
    private var _binding: FragmentNewPetBinding? = null
    private val binding get() = _binding!!
    private var _galleryPicker: GalleryPicker? = null
    private val galleryPicker get() = _galleryPicker!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val photoDao = PetLoggerDatabase.getInstance(application).photoDao
        val viewModelFactory = NewPetViewModelFactory(petDao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(NewPetViewModel::class.java)
        //val datePicker = DatePicker.generate(viewModel.petDOB)

        val galleryViewModelFactory = GalleryViewModelFactory(entityLinker = PetProfilePhotoLinker(photoDao), choiceLimit = 1, photoDao = photoDao)
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)

        _galleryPicker = GalleryPicker(this, binding.galleryPicker, galleryViewModel, associatedID = viewModel.petID)
        galleryPicker.onCreate(savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        galleryViewModel.photosSelected.selection.observe(viewLifecycleOwner, Observer {
            if (it.size > 0) {
                Glide.with(requireContext())
                    .load(it[0].item.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.petPhoto)
            } else binding.petPhoto.setImageResource(R.drawable.placeholder)
        })
        
        binding.petSexSelection.setOnCheckedChangeListener { radioGroup, i ->
            when(binding.petSexSelection.checkedRadioButtonId) {
                binding.petSexMale.id -> viewModel.setPetSex("Male")
                binding.petSexFemale.id -> viewModel.setPetSex("Female")
                binding.petSexOther.id -> viewModel.setPetSex("Other")
                -1 -> viewModel.setPetSex("")
            }
            Log.d("pet_sex_selection", "${binding.petSexSelection.checkedRadioButtonId} : ${viewModel.petSex}")
        }

        binding.submit.setOnClickListener {
            if (viewModel.petName.isNotEmpty()) {
                viewModel.addPet()
                galleryPicker.saveToLocalStorage()
            } else Toast.makeText(requireContext(), R.string.no_pet_name_given, Toast.LENGTH_LONG).show()
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

    override fun onResume() {
        super.onResume()
        galleryPicker.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        galleryPicker.onDestroy()
        _binding = null
    }
}