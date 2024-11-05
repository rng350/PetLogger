package com.hfad.petlogger

import android.Manifest
import android.net.Uri
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
import androidx.core.content.PermissionChecker
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentMediaSelectionBinding
import com.hfad.petlogger.databinding.FragmentMediaSingleSelectionBinding

class MediaSingleSelectionFragment : Fragment() {
    private var _binding: FragmentMediaSingleSelectionBinding? = null
    val binding: FragmentMediaSingleSelectionBinding get() = _binding!!
    val mediaSingleSelectionViewModel: MediaSingleSelectionViewModel by viewModels({requireParentFragment().requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMediaSingleSelectionBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mediaSingleSelectionViewModel = mediaSingleSelectionViewModel

        mediaSingleSelectionViewModel.currentPhoto.observe(viewLifecycleOwner) {photo ->
            if (photo != null) {
                Glide.with(requireContext())
                    .load(photo.contentUri)
                    .into(binding.photoDisplay)
            }
            else {
                binding.photoDisplay.setImageResource(R.drawable.placeholder)
            }
        }

        val pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    mediaSingleSelectionViewModel.pickNewPhoto(requireContext(), uri)
                }
            }

        val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                results ->
            if (        (Build.VERSION.SDK_INT>= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                        && results[Manifest.permission.READ_MEDIA_IMAGES] == true
                        && results[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true)

                ||      (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU
                        && results[Manifest.permission.READ_MEDIA_IMAGES] == true)

                ||      (results[Manifest.permission.READ_EXTERNAL_STORAGE] == true)
            ) {
                launchImagePicker(pickSingleMedia)
            }
            else {
                makePermissionsRequiredToast()
            }
        }

        binding.selectPhotoButton.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if ((ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) == PermissionChecker.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PermissionChecker.PERMISSION_GRANTED)) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ))
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
            }
            else {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }

        binding.resetPhotoSelectionButton.setOnClickListener {
            mediaSingleSelectionViewModel.resetSelection()
        }

        binding.clearPhotoSelectionButton.setOnClickListener {
            mediaSingleSelectionViewModel.removePhoto()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchImagePicker(pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>) {
        pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun makePermissionsRequiredToast() {
        Toast.makeText(requireContext(), "Can't read files without permission.", Toast.LENGTH_LONG).show()
    }
}