package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentGalleryDisplayBinding
import com.hfad.petlogger.databinding.GalleryDisplayItemBinding
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.recyclerviews.DataItemBindingInterface

// for viewing subjects' associated photos (i.e. Pet, Event, Note)
class GalleryDisplay(private val fragment: Fragment,
                     private val binding: FragmentGalleryDisplayBinding,
                     private val viewModel: GalleryDisplayViewModel) {
    private lateinit var adapter: GenericRecyclerViewAdapter<Photo, GalleryDisplayItemBinding>

    fun onCreate(savedInstanceState: Bundle?) {
        Log.e("gallery_display", "oncreate a")
        adapter = GenericRecyclerViewAdapter<Photo, GalleryDisplayItemBinding>(
            layoutId = R.layout.gallery_display_item,
            bindingInterface = createMediaItemBindingInterface()
        )
        Log.e("gallery_display", "oncreate b")
        viewModel.photos.observe(fragment.viewLifecycleOwner, Observer {
            Log.d("photos_observed", "submitting new list")
            adapter.submitList(it.toMutableList())
        })
        Log.e("gallery_display", "oncreate c")
        binding.mediaList.adapter = adapter
        Log.e("gallery_display", "oncreate d")
    }

    private fun createMediaItemBindingInterface()
            = object : DataItemBindingInterface<Photo, GalleryDisplayItemBinding> {
        override fun bind(
            item: Photo,
            binder: GalleryDisplayItemBinding
        ) {
            binder.photo = item

            Glide.with(fragment.requireContext())
                .load(item.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binder.galleryImage)

            // recyclerview-related cleanup to prevent listeners
            binder.galleryCard.setOnClickListener(null)

            binder.galleryCard.setOnClickListener {
                viewModel.photoNavigator.navigateTo(item.id)
            }
        }
    }
}