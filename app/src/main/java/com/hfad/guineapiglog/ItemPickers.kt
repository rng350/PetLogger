package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.guineapiglog.databinding.CheckableEventItemBinding
import com.hfad.guineapiglog.databinding.CheckableWeightItemBinding

object ItemPickers {
    fun setupEventPicker(eventsList: MutableLiveData<MutableList<CheckableItem<Event>>>,
         eventsSelected: SelectionTracker<Event>,
         recyclerView: RecyclerView,
         lifecycleOwner: LifecycleOwner) {
        val adapter = DataItemAdapter<CheckableItem<Event>, CheckableEventItemBinding>(
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

            binder.eventCard.isChecked = item.isChecked

            binder.eventCard.setOnClickListener {
                if (eventsSelected.canSelectMore() || item.isChecked)
                    binder.eventCard.toggle()
                eventsSelected.toggle(item)
            }
        }
    }


    fun setupWeightPicker(weightsList: MutableLiveData<MutableList<CheckableItem<Weight>>>,
                          weightsSelected: SelectionTracker<Weight>,
                          recyclerView: RecyclerView,
                          lifecycleOwner: LifecycleOwner) {
        val adapter = DataItemAdapter<CheckableItem<Weight>, CheckableWeightItemBinding>(
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

            binder.weightCard.isChecked = item.isChecked

            binder.weightCard.setOnClickListener {
                if (eventsSelected.canSelectMore() || item.isChecked)
                    binder.weightCard.toggle()
                eventsSelected.toggle(item)
            }
        }
    }
}