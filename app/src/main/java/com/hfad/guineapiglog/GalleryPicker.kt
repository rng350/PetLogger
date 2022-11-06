package com.hfad.guineapiglog

import android.Manifest
import android.content.ContentProvider
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.guineapiglog.databinding.FragmentGalleryPickerBinding
import com.hfad.guineapiglog.databinding.GalleryPickerItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

// displays photos for picking
class GalleryPicker(private val fragment: Fragment,
                    private val binding: FragmentGalleryPickerBinding,
                    private val viewModel: GalleryViewModel,
                    private val associatedID: LiveData<Long>) {
    private lateinit var permissionsLauncher: ActivityResultLauncher<String>
    private lateinit var contentObserver: ContentObserver
    private lateinit var adapter: DataItemAdapter<CheckableItem<Photo>, GalleryPickerItemBinding>

    fun onCreate(savedInstanceState: Bundle?) {
        updateExternalReadPermission()

        adapter = DataItemAdapter<CheckableItem<Photo>, GalleryPickerItemBinding> (
            layoutId = R.layout.gallery_picker_item,
            bindingInterface = createMediaItemBindingInterface()
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
                viewModel.hasExternalReadPermission.value = true
                // loadPhotosFromExternalStorageToViewModel()
                toggleGalleryPicker() // should be ok here since the activity result is only launched when trying to open the gallery picker
            } else {
                Toast.makeText(fragment.requireContext(), "Can't read files without permission.", Toast.LENGTH_LONG).show()
            }
        }

        binding.galleryPickerButton.setOnClickListener {
            toggleGalleryPicker()
        }

        Log.e("associatedID", "address from GalleryPicker: ${associatedID}")

        viewModel.associatedID.addSource(associatedID) {
            Log.d("assoc_id_changed!", "event id: ${associatedID.value ?: "nil"}")
            viewModel.associatedID.value = it
            viewModel.associatePhotos()
        }
        // because mediatorlivedata needs at least one active observer to do anything
        viewModel.associatedID.observe(fragment.viewLifecycleOwner, Observer {})

        viewModel.allPhotosInsertedToDB.observe(fragment.viewLifecycleOwner, Observer {
            if (it) {
                viewModel.associatePhotos()
            }
        })
    }

    fun onResume() {
        updateExternalReadPermission()
    }

    fun onDestroy() {
        fragment.requireContext().contentResolver.unregisterContentObserver(contentObserver)
    }

    private fun updateExternalReadPermission() {
        viewModel.hasExternalReadPermission.value = ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExternalReadPermission() {
        if (viewModel.hasExternalReadPermission.value != true) {
            permissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
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

    // reaload for when external media has been updated
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
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED
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
                val fileSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateTakenColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                while(cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val size = cursor.getDouble(fileSizeColumn)
                    // TODO: check if every picture has a date taken / date added
                    val date: LocalDateTime? =
                        if (dateTakenColumn != -1) {
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(cursor.getLong(dateTakenColumn)), ZoneOffset.UTC)
                        } else if (dateAddedColumn != -1) {
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(cursor.getLong(dateAddedColumn)), ZoneOffset.UTC)
                        } else null
                    photos.add(CheckableItem<Photo>(Photo(id, displayName, contentUri, width, height, size, date)))
                }
                //Log.d("load", "photo list size: ${photos.size}")
                photos.toList()
            }
                ?: listOf<CheckableItem<Photo>>()
        }
    }

    // TODO: Implement the following check
    // 1. check that there's enough space
    // 2a. if so, try to save files
    // 2b. if not, create a toaster saying there's not enough space
    fun saveToLocalStorage() {
        val savedPhotos = mutableListOf<Photo>()
        viewModel.photosSelected.selection.value?.map { photo ->
            val item = photo.item
            val fileName = generateFilename(item.date)

            var height = 0
            var width = 0

            fragment.requireContext().contentResolver.openInputStream(item.contentUri).use { input ->
                fragment.context!!.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                    val options = BitmapFactory.Options()
                    BitmapFactory.decodeStream(input, null, options)!!.compress(Bitmap.CompressFormat.WEBP, 95, output)
                    height = options.outHeight
                    width = options.outWidth
                }
            }
            val createdFile = File(fragment.context!!.filesDir, fileName)
            if (createdFile.exists()) {
                val fileSize = createdFile.size
                savedPhotos.add(Photo(item.id, item.name, createdFile.toUri(), width, height, fileSize, item.date))
                Log.d("photo_added", "uri: ${createdFile.toUri()}")
                //createdFile.delete() // TODO: comment this out when ready
            }
        }
        viewModel.finalPhotoSelection.value = savedPhotos.toList()
        Log.d("photo_final_selection", "${viewModel.finalPhotoSelection.value}")
        viewModel.onFinalPhotoSelectionUploaded()
    }

    // filename format
    // year-month-day-hour-minute-second-uuid
    // i.e.
    // 20220614_18h22m_[random UUID]
    // 00000000_00h00m_[random UUID]
    fun generateFilename(date: LocalDateTime?): String {
        var prefix = "00000000_00h00m"

        date?.let {
            val yTho = it.year / 1000
            val yHun = (it.year % 1000) / 100
            val yTen = (it.year % 100) / 10
            val yOne = it.year % 10

            val monTen = it.month.value / 10
            val monOne = it.month.value % 10

            val dTen = it.dayOfMonth / 10
            val dOne = it.dayOfMonth % 10

            val hTen = it.hour / 10
            val hOne = it.hour % 10

            val minTen = it.minute / 10
            val minOne = it.minute % 10
            prefix = "${yTho}${yHun}${yTen}${yOne}${monTen}${monOne}${dTen}${dOne}_${hTen}${hOne}h${minTen}${minOne}"
        }

        var fileName = "${prefix}_${UUID.randomUUID()}"
        var hasUnusedFilename = false

        // check if filename is unused
        do {
            if (fileAlreadyExists(fileName))
                fileName = "${prefix}_${UUID.randomUUID()}"
            else hasUnusedFilename = true
        } while (!hasUnusedFilename)

        return fileName
    }

    fun fileAlreadyExists(fileName: String): Boolean {
        Log.d("fileExists", "file: ${fragment.context!!.filesDir}/${fileName}, true/false?? ans: ${File("${fragment.context!!.filesDir}/${fileName}").exists()}")
        return File("${fragment.context!!.filesDir}/${fileName}").exists()
    }

    private fun createMediaItemBindingInterface()
            = object : DataItemBindingInterface<CheckableItem<Photo>, GalleryPickerItemBinding> {
        override fun bind(
            item: CheckableItem<Photo>,
            binder: GalleryPickerItemBinding
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