package com.hfad.petlogger

import android.Manifest
import android.os.Build
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
import com.hfad.petlogger.databinding.FragmentMediaSelectionBinding
import com.hfad.petlogger.photoselection.AdjustablePickMultipleVisualMedia
import com.hfad.petlogger.recyclerviews.SetupPhotoSelectionDisplayUseCase

class MediaSelectionFragment : Fragment() {
    private var _binding: FragmentMediaSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MediaSelectionViewModel by viewModels({requireParentFragment()})

    private lateinit var multiPickupVisualMediaContract: AdjustablePickMultipleVisualMedia
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaSelectionBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        SetupPhotoSelectionDisplayUseCase(
            currentSelection = viewModel.currentPhotoSelection,
            recyclerView = binding.photoList,
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext(),
            photoToggle = { photo -> viewModel.removePhotoFromSelection(photo) }
        )()

        multiPickupVisualMediaContract = AdjustablePickMultipleVisualMedia(viewModel.maxItems - (viewModel.currentPhotoSelection.value?.size ?: 0))

        pickMultipleMedia =
            registerForActivityResult(multiPickupVisualMediaContract) { uris ->
                if (uris.isNotEmpty()) {
                    viewModel.retrievePhotoSelectionFromPickerResults(requireContext(), uris)
                }
            }

        pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    viewModel.retrievePhotoSelectionFromPickerResults(requireContext(), listOf(uri))
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
                launchImagePicker()
            }
            else {
                makePermissionsRequiredToast()
            }
        }

        binding.addPhotosButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if ((ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) == PermissionChecker.PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PermissionChecker.PERMISSION_GRANTED)) {
                    launchImagePicker()
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
                    launchImagePicker()
                }
                else requestPermissions.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
            }
            else {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    launchImagePicker()
                }
                else requestPermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }

        binding.resetButton.setOnClickListener {
            viewModel.resetSelection()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchImagePicker() {
        val maxItems = viewModel.maxItems - (viewModel.currentPhotoSelection.value?.size ?: 0)
        if (maxItems > 1) {
            multiPickupVisualMediaContract.updateMaxItems(maxItems)
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        else if (maxItems == 1) {
            pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        else {
            Toast.makeText(
                requireContext(),
                getString(R.string.photo_selection_limit_reached),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun makePermissionsRequiredToast() {
        Toast.makeText(requireContext(), "Can't read files without permission.", Toast.LENGTH_LONG).show()
    }
}