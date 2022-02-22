package com.hfad.guineapiglog

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PetItemAdapter : RecyclerView.Adapter<PetItemAdapter.PetItemViewHolder>() {
    var data = listOf<Pet>()
    set(value) {
        field = value
        notifyDataSetChanged()
        Log.i("data_size", ""+ data.size)
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        : PetItemViewHolder = PetItemViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: PetItemViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class PetItemViewHolder(val rootView : TextView)
        : RecyclerView.ViewHolder(rootView) {
        companion object {
            fun inflateFrom(parent: ViewGroup): PetItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.pet_item, parent, false) as TextView
                return PetItemViewHolder(view)
            }
        }
        fun bind(item: Pet) {
            rootView.text = item.petName
        }
    }
}