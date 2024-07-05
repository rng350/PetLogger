package com.hfad.petlogger.recyclerviews

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedPetBinding
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.selectiontracker.MultiSelectionTracker

class SetupPetMultiPickerSelectionDisplayUseCase(private val selection: LiveData<List<CheckableItem<PetWithProfilePic>>>,
                                                 private val selectionTracker: MultiSelectionTracker<PetWithProfilePic>,
                                                 private val recyclerView: RecyclerView,
                                                 private val lifecycleOwner: LifecycleOwner,
                                                 private val context: Context
) {

    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<PetWithProfilePic>, ItemSelectedPetBinding>(
            layoutId = R.layout.item_selected_pet,
            bindingInterface = createCheckablePetWithProfilePhotoItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckablePetWithProfilePhotoItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<PetWithProfilePic>, ItemSelectedPetBinding> {
        override fun bind(item: CheckableItem<PetWithProfilePic>, binder: ItemSelectedPetBinding) {
            binder.pet = item.item

            // clear previous requests on viewholder
            Glide.with(context).clear(binder.petProfileImage)

            item.item.profilePic?.let {
                Glide.with(context)
                    .load(it.contentUri)
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