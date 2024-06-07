package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentMediaSelectionBinding
import com.hfad.petlogger.photoselection.AdjustablePickMultipleVisualMedia
import com.hfad.petlogger.recyclerviews.SetupPhotoSelectionDisplayUseCase

class MediaSelectionFragment : Fragment() {
    private var _binding: FragmentMediaSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MediaSelectionViewModel by viewModels({requireParentFragment()})

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

        val multiPickupVisualMediaContract = AdjustablePickMultipleVisualMedia(viewModel.maxItems - (viewModel.currentPhotoSelection.value?.size ?: 0))

        val pickMultipleMedia =
            registerForActivityResult(multiPickupVisualMediaContract) { uris ->
                if (uris.isNotEmpty()) {
                    viewModel.retrievePhotoSelection(requireContext(), uris)
                }
            }

        val pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    viewModel.retrievePhotoSelection(requireContext(), listOf(uri))
                }
            }

        binding.addPhotosButton.setOnClickListener {
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

        binding.resetButton.setOnClickListener {
            viewModel.resetSelection()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}