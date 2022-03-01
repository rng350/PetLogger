package com.hfad.guineapiglog

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hfad.guineapiglog.databinding.EventItemBinding

class EventItemAdapter(val setViewEvent: (eventID: Long) -> Unit, val deleteEvent: (event: Event) -> Unit): RecyclerView.Adapter<EventItemAdapter.EventItemViewHolder>() {
    var data = mutableListOf<Event>()
        set(value) {
            field = value
            notifyDataSetChanged()
            Log.i("data_size", ""+ data.size)
        }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        : EventItemViewHolder = EventItemViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, this, setViewEvent, deleteEvent)
    }

    class EventItemViewHolder(val binding : EventItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): EventItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EventItemBinding.inflate(layoutInflater, parent, false)
                return EventItemViewHolder(binding)
            }
        }

        fun bind(item: Event, adapter: EventItemAdapter, setViewEvent: (eventID: Long) -> Unit, deleteEvent: (event: Event) -> Unit) {
            binding.event = item

            binding.viewEventButton.setOnClickListener {
                setViewEvent(item.eventId)
            }

            binding.deleteEventButton.setOnClickListener {
                deleteEvent(item)
                adapter.notifyItemRemoved(bindingAdapterPosition)
                adapter.notifyItemRangeChanged(bindingAdapterPosition, adapter.data.size)
            }
        }
    }
}