package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentNewPetBinding
import com.hfad.petlogger.entitylinkers.PetProfilePhotoLinker
import com.hfad.petlogger.photoselection.GalleryPicker
import com.hfad.petlogger.photoselection.GalleryViewModel
import com.hfad.petlogger.photoselection.GalleryViewModelFactory


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

        val galleryViewModelFactory = GalleryViewModelFactory(
            entityLinker = PetProfilePhotoLinker(photoDao),
            photoDao = photoDao,
            photosSelected = viewModel.petPhotoSelection
            //photosSelected = viewModel.tempDELETEMEBITCH
        )
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)
        binding.galleryViewModel = galleryViewModel

        _galleryPicker = GalleryPicker(this, binding.galleryPicker, galleryViewModel, associatedID = viewModel.petID)
        galleryPicker.onCreate(savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setAppBarTitle(getString(R.string.new_pet_header))

        galleryViewModel.photosSelected.selectionToAdd.observe(viewLifecycleOwner, Observer {
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
            DatePicker.generate(viewModel.petDOB).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.back.setOnClickListener {
            findNavController().popBackStack()
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