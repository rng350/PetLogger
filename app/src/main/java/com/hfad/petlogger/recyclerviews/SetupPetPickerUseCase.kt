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
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.mutableCopyOf
import com.hfad.petlogger.selectiontracker.SelectionTracker

class SetupPetPickerUseCase(private val petList: MutableLiveData<List<CheckableItem<PetWithProfilePic>>>,
                            private val currentSelection: MutableLiveData<CheckableItem<PetWithProfilePic>>,
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

        petList.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createCheckablePetWithProfilePhotoItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<PetWithProfilePic>, CheckablePetItemBinding> {
        override fun bind(item: CheckableItem<PetWithProfilePic>, binder: CheckablePetItemBinding) {
            binder.checkablePet = item
            binder.pet = item.item

            item.item.profilePic?.let {
                Glide.with(context)
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            }

            binder.petCard.setOnClickListener { null }

            binder.petCard.isChecked = item.isChecked.value!!

            binder.petCard.setOnClickListener {
                updateOldAndNew(item)
            }
        }
    }

    // Single-selection (mandatory)
    private fun updateOldAndNew(newSelected: CheckableItem<PetWithProfilePic>) {
        petList.value?.let { allPets ->
            currentSelection.value?.let { curPetSelected ->
                val oldIndex = allPets.indexOf(curPetSelected)
                val newIndex = allPets.indexOf(newSelected)
                if (oldIndex!=newIndex && oldIndex!=-1 && newIndex!=-1) {
                    val petListCopy = allPets.mutableCopyOf()
                    petListCopy[oldIndex] = petListCopy[oldIndex].copy(isChecked = MutableLiveData(false))
                    petListCopy[newIndex] = petListCopy[newIndex].copy(isChecked = MutableLiveData(true))
                    petList.value = petListCopy
                    currentSelection.value = petListCopy[newIndex]
                }
            }
        }
    }
}