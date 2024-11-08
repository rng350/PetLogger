package com.hfad.petlogger.screens.sections.recyclerviews

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedPhotoBinding
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter

class SetupPhotoSelectionDisplayUseCase(
    private val currentSelection: LiveData<List<Photo>>,
    private val recyclerView: RecyclerView,
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val photoToggle: (photo: Photo) -> Unit
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Photo, ItemSelectedPhotoBinding>(
            layoutId = R.layout.item_selected_photo,
            bindingInterface = createPhotoSelectionDisplayItemBindingInterface()
        )
        recyclerView.adapter = adapter

        currentSelection.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createPhotoSelectionDisplayItemBindingInterface() = object:
        DataItemBindingInterface<Photo, ItemSelectedPhotoBinding> {
        override fun bind(
            item: Photo,
            binder: ItemSelectedPhotoBinding
        ) {
            Glide.with(context).clear(binder.photo)
            item.let {
                Glide.with(context)
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.photo)
            }

            binder.photoCard.setOnClickListener { null }
            binder.photoCard.setOnClickListener {
                photoToggle(item)
            }
        }

    }
}