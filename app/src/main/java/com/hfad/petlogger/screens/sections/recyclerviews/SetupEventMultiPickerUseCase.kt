package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckableEventItemBinding
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.events.EventForList

class SetupEventMultiPickerUseCase(private val eventList: LiveData<List<CheckableItem<EventForList>>>,
                                   private val selection: LiveData<List<EventForList>>,
                                   private val selectionTracker: MultiSelectionTracker<EventForList>,
                                   private val recyclerView: RecyclerView,
                                   private val lifecycleOwner: LifecycleOwner,
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<EventForList>, CheckableEventItemBinding>(
            layoutId = R.layout.checkable_event_item,
            bindingInterface = createCheckableEventItemBindingInterface()
        )
        recyclerView.adapter = adapter

        eventList.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createCheckableEventItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<EventForList>, CheckableEventItemBinding> {
        override fun bind(item: CheckableItem<EventForList>, binder: CheckableEventItemBinding) {
            binder.checkableEvent = item
            binder.event = item.item

            binder.eventCard.isChecked = item.isChecked.value!!

            binder.eventCard.setOnClickListener { null }
            binder.eventCard.setOnClickListener {
                selectionTracker.toggle(item)
            }

            val observer = Observer<List<EventForList>> {
                binder.eventCard.isChecked = selection.value?.contains(item.item) ?: false
            }
            selection.observe(lifecycleOwner, observer)
        }
    }
}