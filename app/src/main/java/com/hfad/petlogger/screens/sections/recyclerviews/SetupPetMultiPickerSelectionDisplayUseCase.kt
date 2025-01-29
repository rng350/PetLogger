package com.hfad.petlogger.screens.sections.recyclerviews

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.databinding.PetItemCellBinding
import com.hfad.petlogger.pets.PetWithProfilePic

class SetupPetMultiPickerSelectionDisplayUseCase(private val selection: LiveData<List<PetWithProfilePic>>,
                                                 private val selectionTracker: MultiSelectionTracker<PetWithProfilePic>,
                                                 private val recyclerView: RecyclerView,
                                                 private val lifecycleOwner: LifecycleOwner,
                                                 private val context: Context
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<PetWithProfilePic, PetItemCellBinding>(
            layoutId = R.layout.pet_item_cell,
            bindingInterface = createCheckablePetWithProfilePhotoItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckablePetWithProfilePhotoItemBindingInterface() = object:
        DataItemBindingInterface<PetWithProfilePic, PetItemCellBinding> {
        override fun bind(
            item: PetWithProfilePic,
            binder: PetItemCellBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.pet = item

            // clear previous requests on viewholder
            Glide.with(context).clear(binder.petProfileImage)

            item.petProfilePicUri?.let {
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            }

            binder.petCard.setOnClickListener { null }
            binder.petCard.setOnClickListener {
                // toggling isn't it... remove from current selection
                selectionTracker.remove(item)
            }
        }
    }
}