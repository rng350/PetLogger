package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.databinding.EventItemBinding
import com.hfad.petlogger.events.EventForList

class SetupEventMultiPickerSelectionDisplayUseCase(private val selection: LiveData<List<EventForList>>,
                                                   private val selectionTracker: MultiSelectionTracker<EventForList>,
                                                   private val recyclerView: RecyclerView,
                                                   private val lifecycleOwner: LifecycleOwner
) {

    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<EventForList, EventItemBinding>(
            layoutId = R.layout.event_item,
            bindingInterface = createCheckableEventItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckableEventItemBindingInterface() = object:
        DataItemBindingInterface<EventForList, EventItemBinding> {
        override fun bind(
            item: EventForList,
            binder: EventItemBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.event = item

            binder.eventCard.setOnClickListener { null }
            binder.eventCard.setOnClickListener {
                selectionTracker.remove(item)
            }
        }
    }
}