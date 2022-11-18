package com.hfad.guineapiglog

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.guineapiglog.databinding.*

object BindingInterfaceCreator {
    fun setupNavigatablePetAdapter(petNavigator: Navigator): DataItemAdapter<Pet, PetItemBinding> {
        return DataItemAdapter<Pet, PetItemBinding>(
            layoutId = R.layout.pet_item,
            bindingInterface = createPetItemBindingInterface(petNavigator)
        )
    }

    private fun createPetItemBindingInterface(petNavigator: Navigator)
            = object : DataItemBindingInterface<Pet, PetItemBinding> {
        override fun bind(
            item: Pet,
            binder: PetItemBinding
        ) {
            binder.pet = item
            binder.viewPetButton.setOnClickListener {
                petNavigator.navigateTo(item.petID)
            }
            binder.deletePetButton.setOnClickListener {
            }
        }
    }

    fun setupNavigatableEventAdapter(eventNavigator: Navigator): DataItemAdapter<Event, EventItemBinding> {
        return DataItemAdapter<Event, EventItemBinding>(
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

    fun setupNavigatableWeightWithPetNameAdapter(weightNavigator: Navigator): DataItemAdapter<WeightWithPetName, WeightItemBinding> {
        return DataItemAdapter<WeightWithPetName, WeightItemBinding>(
            layoutId = R.layout.weight_item,
            bindingInterface = createWeightWithPetNameItemBindingInterface(weightNavigator))
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

    fun setupNavigatableWeightAdapter(weightNavigator: Navigator): DataItemAdapter<Weight, PetWeightItemBinding> {
        return DataItemAdapter<Weight, PetWeightItemBinding>(
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
}