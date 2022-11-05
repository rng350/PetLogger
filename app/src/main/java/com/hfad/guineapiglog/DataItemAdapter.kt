package com.hfad.guineapiglog

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DataItemAdapter<T, U: ViewDataBinding>(
    @LayoutRes val layoutId: Int,
    private val bindingInterface: DataItemBindingInterface<T, U>)
    : ListAdapter<T, DataItemAdapter.DataItemViewHolder>(DataItemDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : DataItemViewHolder {
        return DataItemViewHolder.inflateFrom(parent, layoutId)
    }

    override fun onBindViewHolder(holder: DataItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, bindingInterface)
    }

    class DataItemViewHolder(val binding : ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup, layoutId: Int): DataItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, layoutId, parent, false)
                return DataItemViewHolder(binding)
            }
        }

        fun <T, U: ViewDataBinding>
                bind(
            item: T,
            bindingInterface: DataItemBindingInterface<T, U>) {
            bindingInterface.bind(item, binding as U)
        }
    }
}

interface DataItemBindingInterface<T, U: ViewDataBinding> {
    fun bind(item: T, binder: U)
}

class DataItemDiffUtilCallback<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.toString() == newItem.toString()
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}