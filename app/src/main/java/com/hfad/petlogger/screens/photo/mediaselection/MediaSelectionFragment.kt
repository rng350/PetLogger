package com.hfad.petlogger.screens.photo.mediaselection

import RecyclerViewPaginator
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
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentMediaSelectionBinding
import com.hfad.petlogger.common.photoselection.AdjustablePickMultipleVisualMedia
import com.hfad.petlogger.common.selectiontracker.MediaMultiSelectionTracker
import com.hfad.petlogger.screens.sections.recyclerviews.SetupPhotoSelectionDisplayUseCase

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
            photos = viewModel.currentDisplayedPhotoSelection,
            recyclerView = binding.photoList,
            lifecycleOwner = viewLifecycleOwner,
            lifecycleScope = lifecycleScope,
            context = requireContext(),
            photoToggle = { photo -> viewModel.toggle(photo) }
        )()

        RecyclerViewPaginator(
            recyclerView = binding.photoList,
            onLast = {viewModel.onLastPage()},
            isLoading = {viewModel.isLoading()},
            loadMore = {viewModel.loadMore()}
        )

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onSelectionOptionsQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSelectionOptionsQueryTextChange(newText)
                return true
            }
        })

        viewModel.toKeepButtonChecked.observe(viewLifecycleOwner) { isToggled ->
            val bottomIconRes = if (isToggled) R.drawable.visibility_on_24px else R.drawable.visibility_off_24px
            val bottomIcon = ContextCompat.getDrawable(requireContext(), bottomIconRes)
            binding.toKeepButton.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.selection_to_keep),
                null,
                bottomIcon
            )
        }

        viewModel.toRemoveButtonChecked.observe(viewLifecycleOwner) { isToggled ->
            val bottomIconRes = if (isToggled) R.drawable.visibility_on_24px else R.drawable.visibility_off_24px
            val bottomIcon = ContextCompat.getDrawable(requireContext(), bottomIconRes)
            binding.toRemoveButton.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.selection_to_remove),
                null,
                bottomIcon
            )
        }

        viewModel.toAddButtonChecked.observe(viewLifecycleOwner) { isToggled ->
            val bottomIconRes = if (isToggled) R.drawable.visibility_on_24px else R.drawable.visibility_off_24px
            val bottomIcon = ContextCompat.getDrawable(requireContext(), bottomIconRes)
            binding.toAddButton.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.selection_to_add),
                null,
                bottomIcon
            )
        }

        binding.toKeepButton.setOnClickListener {
            viewModel.toggleToKeepButton()
            setDisplay(
                toKeepButtonIsChecked = binding.toKeepButton.isChecked,
                toRemoveButtonIsChecked = binding.toRemoveButton.isChecked,
                toAddButtonIsChecked = binding.toAddButton.isChecked
            )
        }
        binding.toRemoveButton.setOnClickListener {
            viewModel.toggleToRemoveButton()
            setDisplay(
                toKeepButtonIsChecked = binding.toKeepButton.isChecked,
                toRemoveButtonIsChecked = binding.toRemoveButton.isChecked,
                toAddButtonIsChecked = binding.toAddButton.isChecked
            )
        }
        binding.toAddButton.setOnClickListener {
            viewModel.toggleToAddButton()
            setDisplay(
                toKeepButtonIsChecked = binding.toKeepButton.isChecked,
                toRemoveButtonIsChecked = binding.toRemoveButton.isChecked,
                toAddButtonIsChecked = binding.toAddButton.isChecked
            )
        }

        // 50 is just an arbitrary value -- the max items will be recalculated every time the user tries to launch the image picker
        multiPickupVisualMediaContract = AdjustablePickMultipleVisualMedia(50)

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
        binding.photoList.adapter = null
        _binding = null
    }

    private fun launchImagePicker() {
        val maxItems = viewModel.maxItems - viewModel.selectionSize
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

    private fun setDisplay(
        toKeepButtonIsChecked: Boolean,
        toRemoveButtonIsChecked: Boolean,
        toAddButtonIsChecked: Boolean
    ) {
        if (toKeepButtonIsChecked) {
            if (toRemoveButtonIsChecked) {
                if (toAddButtonIsChecked) {
                    viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.All)
                }
                else {
                    viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.SelectionToKeepAndRemove)
                }
            }
            else if (toAddButtonIsChecked) {
                viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.SelectionToAddAndKeep)
            }
            else {
                viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.SelectionToKeep)
            }
        }
        else if (toRemoveButtonIsChecked) {
            if (toAddButtonIsChecked) {
                viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.SelectionToAddAndRemove)
            }
            else {
                viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.SelectionToRemove)
            }
        }
        else if (toAddButtonIsChecked) {
            viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.SelectionToAdd)
        }
        else {
            viewModel.setDisplayMode(MediaMultiSelectionTracker.Display.None)
        }
    }
}