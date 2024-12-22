package com.hfad.petlogger.screens.sections.recyclerviews

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckablePetItemBinding
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker

class SetupPetMultiPickerUseCase(private val petList: LiveData<List<CheckableItem<PetWithProfilePic>>>,
                                 private val selection: LiveData<List<PetWithProfilePic>>,
                                 private val selectionTracker: MultiSelectionTracker<PetWithProfilePic>,
                                 private val recyclerView: RecyclerView,
                                 private val lifecycleOwner: LifecycleOwner,
                                 private val context: Context
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<PetWithProfilePic>, CheckablePetItemBinding>(
            layoutId = R.layout.checkable_pet_item,
            bindingInterface = createCheckablePetWithProfilePhotoItemBindingInterface()
        )
        recyclerView.adapter = adapter

        Log.d("SetupRV", "Pet list: ${petList.value}")

        petList.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createCheckablePetWithProfilePhotoItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<PetWithProfilePic>, CheckablePetItemBinding> {
        override fun bind(
            item: CheckableItem<PetWithProfilePic>,
            binder: CheckablePetItemBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.checkablePet = item
            binder.pet = item.item

            // clear previous requests on viewholder
            Glide.with(context).clear(binder.petProfileImage)

            item.item.petProfilePicUri?.let {
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            }

            binder.petCard.isChecked = item.isChecked.value!!

            binder.petCard.setOnClickListener { null }
            binder.petCard.setOnClickListener {
                selectionTracker.toggle(item)
            }

            val observer = Observer<List<PetWithProfilePic>> {
                binder.petCard.isChecked = selection.value?.contains(item.item) ?: false
            }
            selection.observe(itemLifecycleOwner, observer)
        }
    }
}