package com.hfad.guineapiglog

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hfad.guineapiglog.databinding.PetItemBinding

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import androidx.lifecycle.viewModelScope

class PetItemAdapter(dao : PetDao) : RecyclerView.Adapter<PetItemAdapter.PetItemViewHolder>() {
    val petDao = dao
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
        holder.bind(item)

        holder.binding.deletePet.setOnClickListener {
            GlobalScope.launch {
                Log.i("REMOVE", "removed at " + position + " length:" + data.size)
                data.removeAt(position)
                // TODO: remove GlobalScope eventually... just testing
                petDao.delete(data[position])
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, data.size)
            }
        }
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
        fun bind(item: Pet) {
            binding.pet = item
        }
    }
}