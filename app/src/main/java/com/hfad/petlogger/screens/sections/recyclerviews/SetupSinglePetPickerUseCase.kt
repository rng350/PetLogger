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
import com.hfad.petlogger.common.selectiontracker.SingleSelectionTracker
import com.hfad.petlogger.databinding.PetItemCellBinding
import com.hfad.petlogger.pets.data.PetWithProfilePic

class SetupSinglePetPickerUseCase(private val petList: LiveData<List<CheckableItem<PetWithProfilePic>>>,
                                  private val currentSelection: LiveData<PetWithProfilePic?>,
                                  private val selectionTracker: SingleSelectionTracker<PetWithProfilePic>,
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

            val observer = Observer<PetWithProfilePic?> {
                binder.petCard.isChecked = item.item.petId==it?.petId
            }
            currentSelection.observe(itemLifecycleOwner, observer)
        }
    }

    // Single-selection (mandatory)
    /*private fun updateOldAndNew(newSelected: CheckableItem<PetWithProfilePic>) {
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
    }*/
}