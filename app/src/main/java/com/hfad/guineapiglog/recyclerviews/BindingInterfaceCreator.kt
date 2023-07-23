package com.hfad.guineapiglog.recyclerviews

import android.content.Context
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
import com.hfad.guineapiglog.entities.Weight
import com.hfad.guineapiglog.entities.WeightWithPetName
import com.hfad.guineapiglog.util.Navigator

object BindingInterfaceCreator {
    fun setupNavigatableEventAdapter(eventNavigator: Navigator): GenericRecyclerViewAdapter<Event, EventItemBinding> {
        return GenericRecyclerViewAdapter<Event, EventItemBinding>(
            layoutId = R.layout.event_item,
            bindingInterface = createEventItemBindingInterface(eventNavigator)
        )
    }

    private fun createEventItemBindingInterface(eventNavigator: Navigator)
            = object : DataItemBindingInterface<Event, EventItemBinding> {
        override fun bind(
            item: Event,
            binder: EventItemBinding
        ) {
            binder.event = item
            binder.viewEventButton.setOnClickListener {
                eventNavigator.navigateTo(item.eventId)
            }
            binder.deleteEventButton.setOnClickListener {
            }
        }
    }

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

    fun setupNavigatableWeightAdapter(weightNavigator: Navigator): GenericRecyclerViewAdapter<Weight, PetWeightItemBinding> {
        return GenericRecyclerViewAdapter<Weight, PetWeightItemBinding>(
            layoutId = R.layout.pet_weight_item,
            bindingInterface = createWeightItemBindingInterface(weightNavigator)
        )
    }

    private fun createWeightItemBindingInterface(weightNavigator: Navigator)
            = object : DataItemBindingInterface<Weight, PetWeightItemBinding> {
        override fun bind(
            item: Weight,
            binder: PetWeightItemBinding
        ) {
            binder.weight = item
            binder.card.setOnClickListener { null }
            binder.card.setOnClickListener {
                weightNavigator.navigateTo(item.id)
            }
        }
    }

    fun setupPetWithProfilePhotoAdapter(petList: MutableLiveData<List<PetWithProfilePic>>,
                                        recyclerView: RecyclerView,
                                        lifecycleOwner: LifecycleOwner,
                                        context: Context,
                                        navigator: Navigator?
    ) {
        val adapter = GenericRecyclerViewAdapter<PetWithProfilePic, PetItemBinding>(
            layoutId = R.layout.pet_item,
            bindingInterface = createPetWithProfilePhotoItemBindingInterface(context, navigator)
        )
        recyclerView.adapter = adapter
        petList.observe(lifecycleOwner, Observer {
            println("Pets...")
            adapter.submitList(it)
            println("Pets: ${it.toString()}")
        })
    }

    private fun createPetWithProfilePhotoItemBindingInterface(context: Context, navigator: Navigator? = null)
            = object: DataItemBindingInterface<PetWithProfilePic, PetItemBinding> {
        override fun bind(item: PetWithProfilePic, binder: PetItemBinding) {
            binder.pet = item

            // clear previous requests on viewholder
            //binder.petProfileImage.setImageBitmap(null)
            Glide.with(context).clear(binder.petProfileImage)

            if (item.profilePic != null) {
                Glide.with(context)
                    .load(item.profilePic.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            } else {
                binder.petProfileImage.setImageResource(R.drawable.placeholder)
            }

            binder.petCard.setOnClickListener { null }

            binder.petCard.setOnClickListener {
                navigator?.navigateTo(item.pet.petID)
            }
        }
    }
}