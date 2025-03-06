package com.hfad.petlogger.common.recyclerviews

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class GenericRecyclerViewAdapter<T, U: ViewDataBinding>(
    @LayoutRes val layoutId: Int,
    private val bindingInterface: DataItemBindingInterface<T, U>
)
    : ListAdapter<T, GenericRecyclerViewAdapter.DataItemViewHolder>(DataItemDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : DataItemViewHolder {
        return DataItemViewHolder.inflateFrom(parent, layoutId)
    }

    override fun onBindViewHolder(holder: DataItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, bindingInterface)
    }

    override fun onViewRecycled(holder: DataItemViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    class DataItemViewHolder(val binding : ViewDataBinding) : RecyclerView.ViewHolder(binding.root), LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

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
            bindingInterface: DataItemBindingInterface<T, U>
        ) {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
            bindingInterface.bind(item, binding as U, this)
        }

        fun onViewRecycled() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }
}

interface DataItemBindingInterface<T, U: ViewDataBinding> {
    fun bind(item: T, binder: U, itemLifecycleOwner: LifecycleOwner)
}

class DataItemDiffUtilCallback<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return oldItem.toString() == newItem.toString()
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return oldItem == newItem
    }
}