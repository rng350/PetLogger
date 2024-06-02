package com.hfad.petlogger.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedEventBinding
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.selectiontracker.EditSelectionTracker

class SetupEventMultiPickerSelectionDisplayUseCase(private val selection: MutableLiveData<List<CheckableItem<Event>>>,
                                                   private val selectionTracker: EditSelectionTracker<Event>,
                                                   private val recyclerView: RecyclerView,
                                                   private val lifecycleOwner: LifecycleOwner
) {

    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Event>, ItemSelectedEventBinding>(
            layoutId = R.layout.item_selected_event,
            bindingInterface = createCheckableEventItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckableEventItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<Event>, ItemSelectedEventBinding> {
        override fun bind(item: CheckableItem<Event>, binder: ItemSelectedEventBinding) {
            binder.event = item.item

            binder.eventCard.setOnClickListener { null }
            binder.eventCard.setOnClickListener {
                val mutableList = selection.value?.toMutableList() ?: mutableListOf<CheckableItem<Event>>()
                selectionTracker.toggle(item)
                mutableList.remove(item)
                selection.value = mutableList.toList()
            }
        }
    }
}