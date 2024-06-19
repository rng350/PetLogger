package com.hfad.petlogger

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentNewNoteBinding
import com.hfad.petlogger.databinding.FragmentNewPhotoBinding
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository

class NewPhotoFragment : Fragment() {
    private var _binding: FragmentNewPhotoBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPhotoBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, requireContext())
        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(database, mediaRepository)
        val eventRepository = EventRepository(database, mediaRepository)
        val weightRepository = WeightRepository(database)

        val newPhotoViewModel = ViewModelProvider(this, NewPhotoViewModel.provideFactory(mediaRepository)).get(NewPhotoViewModel::class.java)
        val petSelectorViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(petRepository)).get(PetMultiSelectionViewModel::class.java)
        val eventSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository)).get(EventMultiSelectionViewModel::class.java)
        val weightMultiSelectionViewModel = ViewModelProvider(this, WeightMultiSelectionViewModel.provideFactory(weightRepository)).get(WeightMultiSelectionViewModel::class.java)
        binding.newPhotoViewModel = newPhotoViewModel
        binding.petSelectorViewModel = petSelectorViewModel
        binding.eventSelectorViewModel = eventSelectionViewModel
        binding.weightSelectorViewModel = weightMultiSelectionViewModel

        newPhotoViewModel.photo.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.photoDisplay)
            } else binding.photoDisplay.setImageResource(R.drawable.placeholder)
        }

        val pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    newPhotoViewModel.setPhoto(requireContext(), uri)
                }
            }

        val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                results ->
            if (        (Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                        && results[READ_MEDIA_IMAGES] == true
                        && results[READ_MEDIA_VISUAL_USER_SELECTED] == true)

                ||      (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU
                        && results[READ_MEDIA_IMAGES] == true)

                ||      (results[READ_EXTERNAL_STORAGE] == true)
            ) {
                launchImagePicker(pickSingleMedia)
            }
            else {
                makePermissionsRequiredToast()
            }
        }

        binding.selectPhotoButton.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if ((ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED)) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED))
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES))
            }
            else {
                if (ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
            }
        }

        binding.clearPhotoSelectionButton.setOnClickListener{
            newPhotoViewModel.resetPhotoSelection()
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.clearButton.setOnClickListener{
            newPhotoViewModel.resetPhotoSelection()
        }

        binding.submitButton.setOnClickListener{
            newPhotoViewModel.submit(
                pets=petSelectorViewModel.getPetsToAdd(),
                events=eventSelectionViewModel.getEventsToAdd(),
                weights = weightMultiSelectionViewModel.getWeightsToAdd()
            )
        }

        newPhotoViewModel.goBack.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigate(NewPhotoFragmentDirections.actionNewPhotoFragmentToFullGalleryFragment())
            }
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun launchImagePicker(pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>) {
        pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun makePermissionsRequiredToast() {
        Toast.makeText(requireContext(), "Can't read files without permission.", Toast.LENGTH_LONG).show()
    }
}