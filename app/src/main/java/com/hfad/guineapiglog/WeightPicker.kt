package com.hfad.guineapiglog

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.guineapiglog.databinding.CheckableEventItemBinding

class WeightPicker(eventsList: MutableLiveData<MutableList<CheckableItem<Weight>>>,
                  private val weightsSelected: SelectionTracker<Weight>,
                  recyclerView: RecyclerView,
                  lifecycleOwner: LifecycleOwner) {
    /*init {
        val adapter = DataItemAdapter<CheckableItem<Weight>, CheckableEventItemBinding>(
            layoutId = R.layout.checkable_weight_item,
            bindingInterface = createCheckableEventItemBindingInterface()
        )
        recyclerView.adapter = adapter

        eventsList.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }*/

    /*private fun createCheckableEventItemBindingInterface()
            = object : DataItemBindingInterface<CheckableItem<Weight>, CheckableEventItemBinding> {
        override fun bind(item: CheckableItem<Weight>, binder: CheckableEventItemBinding) {
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
    }*/
}