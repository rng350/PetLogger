package com.hfad.petlogger.recyclerviews

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedPhotoBinding
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedPhotosDisplayUseCase(private val photos: StateFlow<List<Photo>>,
                                          private val photoNavigator: Navigator,
                                          private val recyclerView: RecyclerView,
                                          private val context: Context,
                                          private val coroutineScope: CoroutineScope
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Photo, ItemSelectedPhotoBinding>(
            layoutId = R.layout.item_selected_photo,
            bindingInterface = createPhotosAssociatedDisplayItemBindingInterface()
        )
        recyclerView.adapter = adapter

        coroutineScope.launch {
            photos.collectLatest {
                adapter.submitList(it)
                Log.d("SetupAssPhotosDisplay", "invoke() - List submitted!")
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