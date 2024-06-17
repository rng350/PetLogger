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
import com.hfad.petlogger.photodisplay.stateless.GetSingleAssociatedItemUseCase
import com.hfad.petlogger.photoselection.GalleryPicker
import com.hfad.petlogger.photoselection.GalleryViewModel
import com.hfad.petlogger.photoselection.GalleryViewModelFactory
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository


class NewPetFragment : Fragment() {
    private var _binding: FragmentNewPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, requireContext())
        val petRepository = PetRepository(database, mediaRepository)
        val newPetViewModel = ViewModelProvider(this, NewPetViewModel.provideFactory(petRepository)).get(NewPetViewModel::class.java)
        binding.newPetViewModel = newPetViewModel

        val profilePicSelectionViewModel = ViewModelProvider(this, MediaSingleSelectionViewModel.provideFactory(mediaRepository = mediaRepository)).get(MediaSingleSelectionViewModel::class.java)
        binding.petProfilePhotoSelectionViewModel = profilePicSelectionViewModel

        val photoMultiSelectionViewModel = ViewModelProvider(this, MediaSelectionViewModel.provideFactory(mediaRepository = mediaRepository)).get(MediaSelectionViewModel::class.java)
        binding.photoSelectionViewModel = photoMultiSelectionViewModel

        setAppBarTitle(getString(R.string.new_pet_header))
        
        binding.petSexSelection.setOnCheckedChangeListener { radioGroup, i ->
            when(binding.petSexSelection.checkedRadioButtonId) {
                binding.petSexMale.id -> newPetViewModel.setPetSex("Male")
                binding.petSexFemale.id -> newPetViewModel.setPetSex("Female")
                binding.petSexOther.id -> newPetViewModel.setPetSex("Other")
                -1 -> newPetViewModel.setPetSex("")
            }
            Log.d("pet_sex_selection", "${binding.petSexSelection.checkedRadioButtonId} : ${newPetViewModel.petSex}")
        }

        binding.submit.setOnClickListener {
            if (newPetViewModel.petName.isNotEmpty()) {
                newPetViewModel.addPet(petProfilePhoto = profilePicSelectionViewModel.currentPhoto.value,  petPhotos = photoMultiSelectionViewModel.getPhotosToAdd())
            } else Toast.makeText(requireContext(), R.string.no_pet_name_given, Toast.LENGTH_LONG).show()
        }

        binding.inputDOBButton.setOnClickListener {
            DatePicker.generate(newPetViewModel.petDOB).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        newPetViewModel.goToViewPet.observe(viewLifecycleOwner) {petId ->
            petId?.let {
                newPetViewModel.goToViewPet.value = null
                profilePicSelectionViewModel.resetSelection()
                photoMultiSelectionViewModel.resetSelection()
                findNavController().navigate(NewPetFragmentDirections.actionNewPetFragmentToViewPetFragment(it))
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}