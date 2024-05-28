package com.hfad.petlogger.recyclerviews

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckablePetItemBinding
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.mutableCopyOf
import com.hfad.petlogger.selectiontracker.EditSelectionTracker
import com.hfad.petlogger.selectiontracker.EditSelectionTrackerMultiPick
import com.hfad.petlogger.selectiontracker.SelectionTracker

class SetupPetMultiPickerUseCase(private val petList: MutableLiveData<List<CheckableItem<PetWithProfilePic>>>,
                                 private val selection: MutableLiveData<List<CheckableItem<PetWithProfilePic>>>,
                                 private val selectionTracker: EditSelectionTracker<PetWithProfilePic>,
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
        override fun bind(item: CheckableItem<PetWithProfilePic>, binder: CheckablePetItemBinding) {
            binder.checkablePet = item
            binder.pet = item.item

            // clear previous requests on viewholder
            Glide.with(context).clear(binder.petProfileImage)

            item.item.profilePic?.let {
                Glide.with(context)
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            }

            binder.petCard.isChecked = item.isChecked.value!!

            binder.petCard.setOnClickListener { null }
            binder.petCard.setOnClickListener {
                val mutableList = selection.value?.toMutableList() ?: mutableListOf<CheckableItem<PetWithProfilePic>>()

                selectionTracker.toggle(item)

                if (mutableList.contains(item)) {
                    mutableList.remove(item)
                    item.isChecked.value = false
                    binder.petCard.isChecked = false
                } else {
                    mutableList.add(item)
                    item.isChecked.value = true
                    binder.petCard.isChecked = true
                }
                selection.value = mutableList.toList()
            }

            val observer = Observer<List<CheckableItem<PetWithProfilePic>>> {
                binder.petCard.isChecked = selection.value?.contains(item) ?: false
            }
            selection.observe(lifecycleOwner, observer)
        }
    }
}