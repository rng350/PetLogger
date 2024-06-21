package com.hfad.petlogger.recyclerviews

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.*
import com.hfad.petlogger.databinding.*
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.selectiontracker.SelectionTracker
import com.hfad.petlogger.selectiontracker.SharedCounterSelectionTracker
import com.hfad.petlogger.selectiontracker.SharedCounterSelectionTrackerCumulative
import com.hfad.petlogger.selectiontracker.SharedCounterSelectionTrackerSubtractive

object ItemPickers {
    fun setupEventPicker(eventsList: MutableLiveData<MutableList<CheckableItem<Event>>>,
                         eventsSelected: SelectionTracker<Event>,
                         recyclerView: RecyclerView,
                         lifecycleOwner: LifecycleOwner) {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Event>, CheckableEventItemBinding>(
            layoutId = R.layout.checkable_event_item,
            bindingInterface = createCheckableEventItemBindingInterface(eventsSelected)
        )
        recyclerView.adapter = adapter

        eventsList.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckableEventItemBindingInterface(eventsSelected: SelectionTracker<Event>)
        = object : DataItemBindingInterface<CheckableItem<Event>, CheckableEventItemBinding> {
        override fun bind(item: CheckableItem<Event>, binder: CheckableEventItemBinding) {
            binder.checkableEvent = item
            binder.event = item.item

            // recyclerview-related cleanup to multiple checks
            binder.eventCard.setOnClickListener { null }

            binder.eventCard.isChecked = item.isChecked.value!!

            binder.eventCard.setOnClickListener {
                eventsSelected.toggle(item)
                binder.eventCard.isChecked = item.isChecked.value!!
            }
        }
    }


    fun setupPetWithProfilePhotoEditPicker(petList: MutableLiveData<List<CheckableItem<PetWithProfilePic>>>,
                                           petsSelected: SelectionTracker<PetWithProfilePic>,
                                           recyclerView: RecyclerView,
                                           lifecycleOwner: LifecycleOwner,
                                           context: Context
    ) {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<PetWithProfilePic>, CheckablePetItemBinding>(
            layoutId = R.layout.checkable_pet_item,
            bindingInterface = createCheckablePetWithProfilePhotoItemBindingInterface(petsSelected, context)
        )
        recyclerView.adapter = adapter

        petList.observe(lifecycleOwner, Observer {
            Log.e("bind", "list submitttttttttttt: List ${it}")
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createCheckablePetWithProfilePhotoItemBindingInterface(
        petsSelected: SelectionTracker<PetWithProfilePic>,
        context: Context
    ) = object:
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
                petsSelected.toggle(item)
                binder.petCard.isChecked = item.isChecked.value!!
            }
        }
    }

}