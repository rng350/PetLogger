package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentEditPetBinding
import com.hfad.petlogger.entitylinkers.PetProfilePhotoLinker
import com.hfad.petlogger.photoselection.GalleryPicker
import com.hfad.petlogger.photoselection.GalleryViewModel
import com.hfad.petlogger.photoselection.GalleryViewModelFactory
import com.hfad.petlogger.recyclerviews.ItemPickers
import java.io.File

class EditPetFragment : Fragment() {
    private var _binding: FragmentEditPetBinding? = null
    private val binding get() = _binding!!
    private var _galleryPicker: GalleryPicker? = null
    private val galleryPicker get() = _galleryPicker!!
    lateinit var editPetViewModel: EditPetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPetBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application

        val petID = EditPetFragmentArgs.fromBundle(requireArguments()).petId

        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val photoDao = PetLoggerDatabase.getInstance(application).photoDao
        val eventDao = PetLoggerDatabase.getInstance(application).eventDao
        val weightDao = PetLoggerDatabase.getInstance(application).weightDao

        val editPetViewModelFactory = EditPetViewModelFactory(petID, petDao, photoDao, eventDao, weightDao)
        editPetViewModel = ViewModelProvider(this, editPetViewModelFactory).get(EditPetViewModel::class.java)
        binding.viewModel = editPetViewModel

        val galleryViewModelFactory = GalleryViewModelFactory(
            entityLinker = PetProfilePhotoLinker(photoDao),
            photoDao = photoDao,
            photosSelected = editPetViewModel.photoSelection)
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)
        binding.galleryViewModel = galleryViewModel

        _galleryPicker = GalleryPicker(this, binding.galleryPicker, galleryViewModel, associatedID = editPetViewModel._petID)
        galleryPicker.onCreate(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        ItemPickers.setupEventPicker(editPetViewModel.events, editPetViewModel.eventsToRemove, binding.eventsList, viewLifecycleOwner)
        ItemPickers.setupWeightPicker(editPetViewModel.weights, editPetViewModel.weightsToRemove, binding.weightsList, viewLifecycleOwner)

        editPetViewModel.pet.observe(viewLifecycleOwner, Observer {
            it?.let {
                val mainActivity = (activity as MainActivity)
                mainActivity.setTopAppBarTitle(it.petName)
                mainActivity.setTopAppBarSubtitle(getString(R.string.editing_details))
            }
        })

        editPetViewModel.petProfilePic.observe(viewLifecycleOwner, Observer {
            // if new pfp hasn't been picked yet
            if (editPetViewModel.newPetProfilePic.value == null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.petPhoto)
            }
        })

        galleryViewModel.photosSelected.selectionToAdd.observe(viewLifecycleOwner, Observer {
            if (it.size > 0) {
                Glide.with(requireContext())
                    .load(it[0].item.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.petPhoto)
            } else {
                editPetViewModel.petProfilePic.value?.let {
                    Glide.with(requireContext())
                        .load(it.contentUri)
                        .apply(RequestOptions().placeholder(R.drawable.placeholder))
                        .into(binding.petPhoto)
                }
            }
        })

        // initialize sex pick
        editPetViewModel.pet.observeOnce(viewLifecycleOwner, Observer {
            when(it.petSex) {
                "Male" -> {
                    binding.petSexMale.isChecked = true
                }
                "Female" -> {
                    binding.petSexFemale.isChecked = true
                }
                "Other" -> {
                    binding.petSexOther.isChecked = true
                }
            }
            editPetViewModel.onPetFetched()
        })

        binding.petSexSelection.setOnCheckedChangeListener { radioGroup, i ->
            when(binding.petSexSelection.checkedRadioButtonId) {
                binding.petSexMale.id -> editPetViewModel.setPetSex("Male")
                binding.petSexFemale.id -> editPetViewModel.setPetSex("Female")
                binding.petSexOther.id -> editPetViewModel.setPetSex("Other")
                -1 -> editPetViewModel.setPetSex("")
            }
        }

        binding.inputDOBButton.setOnClickListener {
            DatePicker.generate(editPetViewModel.newPetDOB).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.submit.setOnClickListener {
            // do all the stuff
            if (galleryViewModel.photosSelected.selectionToAdd.value!!.size > 0) {
                Log.e("saving new pfp", "ssss")
                galleryPicker.saveToLocalStorage()
                // deleteProfilePicFromLocalStorage()
            }
            editPetViewModel.updatePet()

            // TODO: make it so that background tasks are done on a different scope/context so as to be able to continue after you return to the viewpetfrag
            // return to viewpet
            // this.findNavController().navigate(EditPetFragmentDirections.actionEditPetFragmentToViewPetFragment(petID))
        }

        binding.cancel.setOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.delete.setOnClickListener {
            // dialogfragment asking for confirmation
            // if yes, delete
            deleteProfilePicFromLocalStorage()
            // go back to home
            this.findNavController().navigate(R.id.action_editPetFragment_to_homeFragment)
        }

        return view
    }

    private fun deleteProfilePicFromLocalStorage() {
        editPetViewModel.petProfilePic.value?.let { photo ->
            photo.contentUri.path?.let { path ->
                File(path).delete()
            }
        }
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