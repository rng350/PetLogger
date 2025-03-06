package com.hfad.petlogger.screens.sections.recyclerviews

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.databinding.PetItemCellBinding
import com.hfad.petlogger.pets.data.PetWithProfilePic

class SetupPetMultiPickerUseCase(private val petList: LiveData<List<CheckableItem<PetWithProfilePic>>>,
                                 private val selection: LiveData<List<PetWithProfilePic>>,
                                 private val selectionTracker: MultiSelectionTracker<PetWithProfilePic>,
                                 private val recyclerView: RecyclerView,
                                 private val lifecycleOwner: LifecycleOwner,
                                 private val context: Context
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<PetWithProfilePic>, PetItemCellBinding>(
            layoutId = R.layout.pet_item_cell,
            bindingInterface = createCheckablePetWithProfilePhotoItemBindingInterface()
        )
        recyclerView.adapter = adapter

        petList.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createCheckablePetWithProfilePhotoItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<PetWithProfilePic>, PetItemCellBinding> {
        override fun bind(
            item: CheckableItem<PetWithProfilePic>,
            binder: PetItemCellBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.petCard.isCheckable = true
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