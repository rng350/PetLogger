package com.hfad.guineapiglog

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hfad.guineapiglog.databinding.WeightItemBinding

class WeightItemAdapter(val setViewWeight: (weightID: Long) -> Unit, val deleteWeight: (weight: Weight) -> Unit): RecyclerView.Adapter<WeightItemAdapter.WeightItemViewHolder>() {
    var data = mutableListOf<WeightWithPetName>()
        set(value) {
            field = value
            notifyDataSetChanged()
            Log.i("data_size", ""+ data.size)
        }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : WeightItemViewHolder = WeightItemViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: WeightItemViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, this, setViewWeight, deleteWeight)
    }

    class WeightItemViewHolder(val binding : WeightItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): WeightItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = WeightItemBinding.inflate(layoutInflater, parent, false)
                return WeightItemViewHolder(binding)
            }
        }

        fun bind(item: WeightWithPetName, adapter: WeightItemAdapter, setViewWeight: (weightID: Long) -> Unit, deleteWeight: (weight: Weight) -> Unit) {
            binding.weight = item

            binding.viewWeightButton.setOnClickListener {
                setViewWeight(item.weight.id)
            }

            binding.deleteWeightButton.setOnClickListener {
                deleteWeight(item.weight)
                adapter.notifyItemRemoved(bindingAdapterPosition)
                adapter.notifyItemRangeChanged(bindingAdapterPosition, adapter.data.size)
            }
        }
    }
}