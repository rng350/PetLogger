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
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedEventsDisplayUseCase(private val events: StateFlow<List<Event>>,
                                          private val eventNavigator: Navigator,
                                          private val recyclerView: RecyclerView,
                                          private val lifecycleScope: LifecycleCoroutineScope,
                                          private val lifecycleOwner: LifecycleOwner) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Event, EventItemBinding>(
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
            = object : DataItemBindingInterface<Event, EventItemBinding> {
        override fun bind(
            item: Event,
            binder: EventItemBinding
        ) {
            binder.event = item
            binder.viewEventButton.setOnClickListener { null }
            binder.viewEventButton.setOnClickListener {
                eventNavigator.navigateTo(item.eventId)
            }
        }
    }
}