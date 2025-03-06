package com.hfad.petlogger.screens.sections.recyclerviews

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.databinding.ItemPhotoBinding
import com.hfad.petlogger.photos.data.Photo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedPhotosDisplayUseCase(private val photos: StateFlow<List<Photo>>,
                                          private val photoNavigator: Navigator,
                                          private val recyclerView: RecyclerView,
                                          private val context: Context,
                                          private val lifecycleScope: LifecycleCoroutineScope,
                                          private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Photo, ItemPhotoBinding>(
            layoutId = R.layout.item_photo,
            bindingInterface = createPhotosAssociatedDisplayItemBindingInterface()
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photos.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
    }
    private fun createPhotosAssociatedDisplayItemBindingInterface() = object:
        DataItemBindingInterface<Photo, ItemPhotoBinding> {
        override fun bind(
            item: Photo,
            binder: ItemPhotoBinding,
            itemLifecycleOwner: LifecycleOwner
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
                photoNavigator.navigateTo(item.id)
            }
        }
    }
}