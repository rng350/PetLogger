package com.hfad.guineapiglog

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hfad.guineapiglog.databinding.PetItemBinding

class PetItemAdapter(val setViewPet: (petId: Long) -> Unit, val deletePet: (pet: Pet) -> Unit) : RecyclerView.Adapter<PetItemAdapter.PetItemViewHolder>() {

    var data = mutableListOf<Pet>()
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
        holder.bind(item, this, setViewPet, deletePet)
    }

    class PetItemViewHolder(val binding : PetItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): PetItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PetItemBinding.inflate(layoutInflater, parent, false)
                return PetItemViewHolder(binding)
            }
        }
        fun bind(item: Pet, adapter: PetItemAdapter, setViewPet: (petId: Long) -> Unit, deletePet: (pet: Pet) -> Unit) {
            binding.pet = item

            binding.deletePetButton.setOnClickListener {
                deletePet(item)
                adapter.notifyItemRemoved(bindingAdapterPosition)
                adapter.notifyItemRangeChanged(bindingAdapterPosition, adapter.data.size)
            }

            binding.viewPetButton.setOnClickListener {
                setViewPet(item.petID)
            }
        }
    }
}