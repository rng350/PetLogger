package com.hfad.petlogger.screens.sections.recyclerviews

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.databinding.ItemSelectedPhotoBinding
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupPhotoSelectionDisplayUseCase(
    private val photos: StateFlow<List<CheckableItem<Photo>>>,
    private val recyclerView: RecyclerView,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val photoToggle: (photo: CheckableItem<Photo>) -> Unit
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Photo>, ItemSelectedPhotoBinding>(
            layoutId = R.layout.item_selected_photo,
            bindingInterface = createPhotoSelectionDisplayItemBindingInterface()
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

    private fun createPhotoSelectionDisplayItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<Photo>, ItemSelectedPhotoBinding> {
        override fun bind(
            item: CheckableItem<Photo>,
            binder: ItemSelectedPhotoBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.checkablePhoto = item
            Glide.with(context).clear(binder.photo)
            item.let {
                Glide.with(context)
                    .load(it.item.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.photo)
            }

            item.isChecked.observe(itemLifecycleOwner, Observer {
                binder.photoCard.isChecked = it
            })

            binder.photoCard.setOnClickListener(null)
            binder.photoCard.setOnClickListener {
                photoToggle(item)
            }
        }
    }
}