package com.hfad.guineapiglog

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.guineapiglog.databinding.FragmentGalleryPickerBinding
import com.hfad.guineapiglog.databinding.GalleryPickerItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// displays photos for picking
class GalleryPicker(private val fragment: Fragment, private val binding: FragmentGalleryPickerBinding, private val viewModel: GalleryViewModel) {
    private lateinit var permissionsLauncher: ActivityResultLauncher<String>
    private lateinit var contentObserver: ContentObserver
    private lateinit var adapter: DataItemAdapter<CheckableItem<Photo>, GalleryPickerItemBinding>

    fun onCreate(savedInstanceState: Bundle?) {
        updateExternalReadPermission()

        adapter = DataItemAdapter<CheckableItem<Photo>, GalleryPickerItemBinding> (
            layoutId = R.layout.gallery_picker_item,
            bindingInterface = createMediaItemBindingInterface(),
            listItems = viewModel.allExternalPhotos.value!!.toMutableList(),
            setViewData = {  },
            deleteData = {  }
        )
        viewModel.allExternalPhotos.observe(fragment.viewLifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
        binding.mediaList.adapter = adapter

        initContentObserver()

        // remove photo list if permission revoked
        viewModel.hasExternalReadPermission.observe(fragment.viewLifecycleOwner, Observer {
            if (!it) {
                viewModel.allExternalPhotos.value = mutableListOf<CheckableItem<Photo>>()
            }
        })

        permissionsLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted: Boolean ->
            if (isGranted) {
                loadPhotosFromExternalStorageToViewModel()
            } else {
                Toast.makeText(fragment.requireContext(), "Can't read files without permission.", Toast.LENGTH_LONG).show()
            }
        }

        binding.galleryPickerButton.setOnClickListener {
            toggleGalleryPicker()
        }
    }

    fun onResume() {
        updateExternalReadPermission()
    }

    private fun updateExternalReadPermission() {
        viewModel.hasExternalReadPermission.value = ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExternalReadPermission(): Boolean {
        if (viewModel.hasExternalReadPermission.value != true) {
            permissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            return (viewModel.hasExternalReadPermission.value == true)
        } else return true
    }

    private fun toggleGalleryPicker() {
        //Log.d("t_photos", "list size: ${viewModel.allExternalPhotos.value?.size ?: "0"}")
        if (binding.mediaList.visibility == View.VISIBLE) {
            binding.mediaList.visibility = View.GONE
        }
        else {
            if (viewModel.hasExternalReadPermission.value == true) {
                binding.mediaList.visibility = View.VISIBLE
                // if is granted but gallery hasn't been loaded...
                if (viewModel.allExternalPhotos.value?.size == 0) {
                    loadPhotosFromExternalStorageToViewModel()
                }
            }
            else {
                requestExternalReadPermission()
            }
        }
    }

    // for when external media has been updated
    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                if(viewModel.hasExternalReadPermission.value == true) {
                    loadPhotosFromExternalStorageToViewModel()
                }
            }
        }
        fragment.requireContext().contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    private fun loadPhotosFromExternalStorageToViewModel() {
        viewModel.viewModelScope.launch {
            viewModel.allExternalPhotos.value = loadPhotosFromExternalStorage()
        }
    }

    private suspend fun loadPhotosFromExternalStorage(): List<CheckableItem<Photo>> {
        return withContext(Dispatchers.IO) {
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )

            val photos = mutableListOf<CheckableItem<Photo>>()
            fragment.requireContext().contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while(cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add(CheckableItem<Photo>(Photo(id, displayName, contentUri, width, height, notes="")))
                }
                //Log.d("load", "photo list size: ${photos.size}")
                photos.toList()
            }
                ?: listOf<CheckableItem<Photo>>()
        }
    }

    // TODO: Implement
    private fun saveToLocalStorage() {

    }

    private fun createMediaItemBindingInterface()
            = object : DataItemBindingInterface<CheckableItem<Photo>, GalleryPickerItemBinding> {
        override fun bind(
            item: CheckableItem<Photo>,
            binder: GalleryPickerItemBinding,
            setViewData: (id: Long) -> Unit,
            deleteData: (toDelete: CheckableItem<Photo>) -> Unit
        ) {
            binder.photo = item

            Glide.with(fragment.requireContext())
                .load(item.item.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binder.galleryImage)

            // recyclerview-related cleanup to multiple checks
            binder.galleryCard.setOnClickListener { null }
            // cleanup for single choice selection only
            if (viewModel.choiceLimit == 1) {
                val prevBindingObserver = viewModel.selected[binder]
                prevBindingObserver?.let {
                    viewModel.photosSelected.selection.removeObserver(it)
                }
            }

            binder.galleryCard.isChecked = item.isChecked

            binder.galleryCard.setOnClickListener {
                if (viewModel.canSelectMore() || binder.galleryCard.isChecked)
                    binder.galleryCard.toggle()
                viewModel.toggle(item)
            }

            if (viewModel.choiceLimit == 1) {
                // observer into variable for reuse
                val observer = Observer<MutableList<CheckableItem<Photo>>> {
                    if (!it.contains(item)) {
                        binder.galleryCard.isChecked = false
                    }
                }
                viewModel.photosSelected.selection.observe(fragment.viewLifecycleOwner, observer)
                viewModel.selected[binder] = observer
            }
            // TODO: get rid of recyclerviews altogether and replace with LazyColumns (compose)
        }
    }
}