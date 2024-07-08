package com.hfad.petlogger.recyclerviews

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.*
import com.hfad.petlogger.databinding.*
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.Navigator

object BindingInterfaceCreator {
    fun setupNavigatableWeightWithPetNameAdapter(weightNavigator: Navigator): GenericRecyclerViewAdapter<WeightWithPetName, WeightItemBinding> {
        return GenericRecyclerViewAdapter<WeightWithPetName, WeightItemBinding>(
            layoutId = R.layout.weight_item,
            bindingInterface = createWeightWithPetNameItemBindingInterface(weightNavigator)
        )
    }

    private fun createWeightWithPetNameItemBindingInterface(weightNavigator: Navigator)
            = object : DataItemBindingInterface<WeightWithPetName, WeightItemBinding> {
        override fun bind(
            item: WeightWithPetName,
            binder: WeightItemBinding
        ) {
            binder.weight = item
            binder.viewWeightButton.setOnClickListener {
                weightNavigator.navigateTo(item.weight.id)
            }
            binder.deleteWeightButton.setOnClickListener {
            }
        }
    }

    fun setupGalleryPhotoItemAdapter(photoList: MutableLiveData<List<Photo>>,
                                     recyclerView: RecyclerView,
                                     lifecycleOwner: LifecycleOwner,
                                     context: Context,
                                     navigator: Navigator?) {
        val adapter = GenericRecyclerViewAdapter<Photo, GalleryDisplayItemBinding>(
            layoutId = R.layout.gallery_display_item,
            bindingInterface = createGalleryPhotoItemBindingInterface(context, navigator)
        )
        Log.i("setupGalleryPhItemAda", "before adapter set")
        recyclerView.adapter = adapter
        photoList.observe(lifecycleOwner, Observer {
            Log.i("setupGalleryPhItemAda", "adapter list submitted...")
            adapter.submitList(it)
        })
    }

    private fun createGalleryPhotoItemBindingInterface(context: Context, navigator: Navigator? = null)
            = object : DataItemBindingInterface<Photo, GalleryDisplayItemBinding> {
        override fun bind(
            item: Photo,
            binder: GalleryDisplayItemBinding
        ) {
            binder.photo = item

            Glide.with(context)
                .load(item.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binder.galleryImage)

            // recyclerview-related cleanup to prevent listeners
            binder.galleryCard.setOnClickListener(null)

            binder.galleryCard.setOnClickListener {
                navigator?.navigateTo(item.id)
            }
        }
    }


    fun setupNoteListItemAdapter(noteList: MutableLiveData<List<Note>>,
                                 recyclerView: RecyclerView,
                                 lifecycleOwner: LifecycleOwner,
                                 navigator: Navigator) {
        val adapter = GenericRecyclerViewAdapter<Note, NoteShortItemBinding>(
            layoutId = R.layout.note_short_item,
            bindingInterface = createNoteListItemBindingInterface(navigator)
        )
        recyclerView.adapter = adapter
        noteList.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createNoteListItemBindingInterface(navigator: Navigator)
            = object : DataItemBindingInterface<Note,NoteShortItemBinding> {
        override fun bind(
            item: Note,
            binder: NoteShortItemBinding
        ) {
            binder.note = item

            // recyclerview-related cleanup to prevent listeners
            binder.noteCard.setOnClickListener(null)

            binder.noteCard.setOnClickListener {
                navigator.navigateTo(item.id)
            }
        }
    }
}