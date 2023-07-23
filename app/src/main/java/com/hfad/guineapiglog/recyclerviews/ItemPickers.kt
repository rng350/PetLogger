package com.hfad.guineapiglog.recyclerviews

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.guineapiglog.*
import com.hfad.guineapiglog.databinding.*
import com.hfad.guineapiglog.entities.Event
import com.hfad.guineapiglog.entities.PetWithProfilePic
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.entities.Weight
import com.hfad.guineapiglog.selectiontracker.SelectionTracker
import com.hfad.guineapiglog.selectiontracker.SharedCounterSelectionTracker
import com.hfad.guineapiglog.selectiontracker.SharedCounterSelectionTrackerCumulative
import com.hfad.guineapiglog.selectiontracker.SharedCounterSelectionTrackerSubtractive

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


    fun setupWeightPicker(weightsList: MutableLiveData<MutableList<CheckableItem<Weight>>>,
                          weightsSelected: SelectionTracker<Weight>,
                          recyclerView: RecyclerView,
                          lifecycleOwner: LifecycleOwner) {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Weight>, CheckableWeightItemBinding>(
            layoutId = R.layout.checkable_weight_item,
            bindingInterface = createCheckableWeightItemBindingInterface(weightsSelected)
        )
        recyclerView.adapter = adapter

        weightsList.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckableWeightItemBindingInterface(eventsSelected: SelectionTracker<Weight>)
            = object : DataItemBindingInterface<CheckableItem<Weight>, CheckableWeightItemBinding> {
        override fun bind(item: CheckableItem<Weight>, binder: CheckableWeightItemBinding) {
            binder.checkableEvent = item
            binder.weight = item.item

            // recyclerview-related cleanup to multiple checks
            binder.weightCard.setOnClickListener { null }

            binder.weightCard.isChecked = item.isChecked.value!!

            binder.weightCard.setOnClickListener {
                eventsSelected.toggle(item)
                binder.weightCard.isChecked = item.isChecked.value!!
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

    fun setupGalleryNewPicker(photoList: MutableLiveData<List<CheckableItem<Photo>>>,
                              recyclerView: RecyclerView,
                           // this here can be
                           // NewSelectionTrackerMultiPick,
                           // NewSelectionTrackerSinglePick
                           // or SharedCounterSelectionTrackerCumulative
                              photosSelected: SelectionTracker<Photo>,
                              lifecycleOwner: LifecycleOwner,
                              context: Context,
                              selected: HashMap<GalleryPickerItemBinding, Observer<Boolean>>
    ) {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Photo>, GalleryPickerItemBinding>(
            layoutId = R.layout.gallery_picker_item,
            bindingInterface = createGalleryPickerItemBindingInterface(photosSelected, context, lifecycleOwner, selected)
        )
        recyclerView.adapter = adapter

        photoList.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createGalleryPickerItemBindingInterface(
        photosSelected: SelectionTracker<Photo>,
        context: Context,
        lifecycleOwner: LifecycleOwner,
        selected: HashMap<GalleryPickerItemBinding, Observer<Boolean>>)
            = object : DataItemBindingInterface<CheckableItem<Photo>, GalleryPickerItemBinding> {
        override fun bind(
            item: CheckableItem<Photo>,
            binder: GalleryPickerItemBinding
        ) {
            binder.photo = item

            Glide.with(context)
                .load(item.item.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binder.galleryImage)

            // recyclerview-related cleanup to multiple checks
            binder.galleryCard.setOnClickListener { null }

            // cleanup for deselection from external factors
            val prevBindingObserver = selected[binder]
            prevBindingObserver?.let {
                item.isChecked.removeObserver(it)
            }

            binder.galleryCard.isChecked = item.isChecked.value!!

            binder.galleryCard.setOnClickListener {
                photosSelected.toggle(item)
                binder.galleryCard.isChecked = item.isChecked.value!!
            }

            // setup for deselection from external factors
            // observer into variable for reuse
            val observer = Observer<Boolean> {
                requireNotNull(it)
                binder.galleryCard.isChecked = it
            }
            item.isChecked.observe(lifecycleOwner, observer)
            selected[binder] = observer
        }
    }

    fun setupOldPhotoDeselector(
        photoList: MutableLiveData<List<CheckableItem<Photo>>>,
        recyclerView: RecyclerView,
        photosDeselected: SharedCounterSelectionTracker<Photo>,
        lifecycleOwner: LifecycleOwner,
        context: Context,
    ) {
        require(photosDeselected.tracker is SharedCounterSelectionTrackerSubtractive)
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Photo>, CheckableEditPhotoDisplayDeleteBinding>(
            layoutId = R.layout.checkable_edit_photo_display_delete,
            bindingInterface = createOldPhotoDeselectionItemBindingInterface(photosDeselected, context)
        )
        recyclerView.adapter = adapter

        photoList.observe(lifecycleOwner, Observer {
            Log.e("oldphoto", "old photo deselected! or just setup...")
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createOldPhotoDeselectionItemBindingInterface(
        photosDeselected: SharedCounterSelectionTracker<Photo>,
        context: Context
    ) = object:
        DataItemBindingInterface<CheckableItem<Photo>, CheckableEditPhotoDisplayDeleteBinding> {
        override fun bind(
            item: CheckableItem<Photo>,
            binder: CheckableEditPhotoDisplayDeleteBinding
        ) {
            require(photosDeselected.tracker is SharedCounterSelectionTrackerSubtractive)
            binder.checkableItem = item
            binder.photoItem = item.item

            binder.photoCard.isChecked = item.isChecked.value!!

            item.item.let {
                Glide.with(context)
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.photo)
            }

            binder.photoCard.setOnClickListener { null }
            binder.photoCard.setOnClickListener {
                photosDeselected.toggle(item)
                binder.photoCard.isChecked = item.isChecked.value!!
            }
        }

    }

    fun setupNewPhotoDeselector(
        recyclerView: RecyclerView,
        photosSelected: SharedCounterSelectionTracker<Photo>,
        lifecycleOwner: LifecycleOwner,
        context: Context,
    ) {
        require(photosSelected.tracker is SharedCounterSelectionTrackerCumulative)

        val adapter = GenericRecyclerViewAdapter<CheckableItem<Photo>, CheckableEditPhotoDisplayNewBinding>(
            layoutId = R.layout.checkable_edit_photo_display_new,
            bindingInterface = createNewPhotoDeselectionItemBindingInterface(photosSelected, context)
        )
        recyclerView.adapter = adapter

        photosSelected.tracker.selection.observe(lifecycleOwner, Observer {
            Log.e("newphoto", "new photo deselected! or just setup...")
            adapter.submitList(it)
        })
    }

    private fun createNewPhotoDeselectionItemBindingInterface(
        photosSelected: SharedCounterSelectionTracker<Photo>,
        context: Context,
    ) = object:
        DataItemBindingInterface<CheckableItem<Photo>, CheckableEditPhotoDisplayNewBinding> {
        override fun bind(item: CheckableItem<Photo>, binder: CheckableEditPhotoDisplayNewBinding) {
            require(photosSelected.tracker is SharedCounterSelectionTrackerCumulative)

            binder.photoItem = item.item

            item.item.let {
                Glide.with(context)
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.photo)
            }

            binder.photoCard.setOnClickListener { null }
            binder.photoCard.setOnClickListener {
                photosSelected.remove(item)
            }
        }
    }
}