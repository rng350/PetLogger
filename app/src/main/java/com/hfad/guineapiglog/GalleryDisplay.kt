package com.hfad.guineapiglog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.guineapiglog.databinding.FragmentGalleryDisplayBinding
import com.hfad.guineapiglog.databinding.GalleryDisplayItemBinding

// for viewing subjects' associated photos (i.e. Pet, Event, Note)
class GalleryDisplay(private val fragment: Fragment,
                     private val binding: FragmentGalleryDisplayBinding,
                     private val viewModel: GalleryDisplayViewModel) {
    private lateinit var adapter: DataItemAdapter<Photo, GalleryDisplayItemBinding>

    fun onCreate(savedInstanceState: Bundle?) {
        Log.e("gallery_display", "oncreate a")
        adapter = DataItemAdapter<Photo, GalleryDisplayItemBinding>(
            layoutId = R.layout.gallery_display_item,
            bindingInterface = createMediaItemBindingInterface(),
            listItems = viewModel.photos.value!!.toMutableList(),
            setViewData = {  },
            deleteData = {  }
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
            binder: GalleryDisplayItemBinding,
            setViewData: (id: Long) -> Unit,
            deleteData: (toDelete: Photo) -> Unit
        ) {
            binder.photo = item

            Log.d("adapter_bind", "Are we even there?")

            Glide.with(fragment.requireContext())
                .load(item.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binder.galleryImage)

            // recyclerview-related cleanup to prevent listeners
            binder.galleryCard.setOnClickListener { null }

            binder.galleryCard.setOnClickListener {
                // TODO: need to implement ViewPhotoFragment & ViewModel, navgraph stuff
                viewModel.photoNavigator.navigateTo(item.id)
            }
            // TODO: get rid of recyclerviews altogether and replace with LazyColumns (compose)
        }
    }
}