package com.hfad.petlogger.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckableEventItemBinding
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.selectiontracker.EditSelectionTracker

class SetupEventMultiPickerUseCase(private val eventList: MutableLiveData<List<CheckableItem<Event>>>,
                                   private val selection: MutableLiveData<List<CheckableItem<Event>>>,
                                   private val selectionTracker: EditSelectionTracker<Event>,
                                   private val recyclerView: RecyclerView,
                                   private val lifecycleOwner: LifecycleOwner,
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Event>, CheckableEventItemBinding>(
            layoutId = R.layout.checkable_event_item,
            bindingInterface = createCheckableEventItemBindingInterface()
        )
        recyclerView.adapter = adapter

        eventList.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createCheckableEventItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<Event>, CheckableEventItemBinding> {
        override fun bind(item: CheckableItem<Event>, binder: CheckableEventItemBinding) {
            binder.checkableEvent = item
            binder.event = item.item
            binder.eventCard.isChecked = item.isChecked.value!!

            binder.eventCard.setOnClickListener { null }
            binder.eventCard.setOnClickListener {
                val mutableList = selection.value?.toMutableList() ?: mutableListOf<CheckableItem<Event>>()

                selectionTracker.toggle(item)

                if (mutableList.contains(item)) {
                    mutableList.remove(item)
                    item.isChecked.value = false
                    binder.eventCard.isChecked = false
                } else {
                    mutableList.add(item)
                    item.isChecked.value = true
                    binder.eventCard.isChecked = true
                }
                selection.value = mutableList.toList()
            }

            val observer = Observer<List<CheckableItem<Event>>> {
                binder.eventCard.isChecked = selection.value?.contains(item) ?: false
            }
            selection.observe(lifecycleOwner, observer)
        }
    }
}