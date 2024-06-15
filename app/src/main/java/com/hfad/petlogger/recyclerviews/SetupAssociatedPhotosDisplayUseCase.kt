package com.hfad.petlogger.recyclerviews

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedPhotoBinding
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.util.Navigator
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
        val adapter = GenericRecyclerViewAdapter<Photo, ItemSelectedPhotoBinding>(
            layoutId = R.layout.item_selected_photo,
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
                photoNavigator.navigateTo(item.id)
            }
        }
    }
}