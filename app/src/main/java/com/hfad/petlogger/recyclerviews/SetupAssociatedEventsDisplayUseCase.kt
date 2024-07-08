package com.hfad.petlogger.recyclerviews

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.EventItemBinding
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedEventsDisplayUseCase(private val events: StateFlow<List<EventForList>>,
                                          private val eventNavigator: Navigator,
                                          private val recyclerView: RecyclerView,
                                          private val lifecycleScope: LifecycleCoroutineScope,
                                          private val lifecycleOwner: LifecycleOwner) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<EventForList, EventItemBinding>(
            layoutId = R.layout.event_item,
            bindingInterface = createEventItemBindingInterface(eventNavigator)
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                events.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun createEventItemBindingInterface(eventNavigator: Navigator)
            = object : DataItemBindingInterface<EventForList, EventItemBinding> {
        override fun bind(
            item: EventForList,
            binder: EventItemBinding
        ) {
            binder.event = item
            binder.eventCard.setOnClickListener { null }
            binder.eventCard.setOnClickListener { eventNavigator.navigateTo(item.eventId) }
        }
    }
}