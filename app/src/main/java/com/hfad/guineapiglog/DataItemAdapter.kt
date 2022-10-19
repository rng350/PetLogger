package com.hfad.guineapiglog

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.ItemKeyProvider.SCOPE_CACHED
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DataItemAdapter<T, U: ViewDataBinding>(
    @LayoutRes val layoutId: Int,
    private val bindingInterface: DataItemBindingInterface<T, U>,
    private var listItems: MutableList<T>,
    val setViewData: (dataID: Long) -> Unit,
    val deleteData: (data: T) -> Unit)
        : ListAdapter<T, DataItemAdapter.DataItemViewHolder<T>>(DataItemDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : DataItemViewHolder<T> {
        return DataItemViewHolder.inflateFrom(parent, layoutId, listItems)
    }

    override fun onBindViewHolder(holder: DataItemViewHolder<T>, position: Int) {
        val item = getItem(position)
        holder.bind(item, bindingInterface, setViewData, deleteData)
    }

    override fun submitList(list: MutableList<T>?) {
        listItems = list ?: mutableListOf<T>()
        super.submitList(list)
    }

    override fun onViewRecycled(holder: DataItemViewHolder<T>) {
        super.onViewRecycled(holder)
    }

    class DataItemViewHolder<T>(val binding : ViewDataBinding, val listItems: List<T>) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun <T>inflateFrom(parent: ViewGroup, layoutId: Int, listItems: List<T>): DataItemViewHolder<T> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, layoutId, parent, false)
                return DataItemViewHolder(binding, listItems)
            }
        }

        fun <T, U: ViewDataBinding> bind(
                    item: T,
                    bindingInterface: DataItemBindingInterface<T, U>,
                    setViewData: (id: Long) -> Unit,
                    deleteData: (toDelete: T) -> Unit) {
            bindingInterface.bind(item, binding as U, setViewData, deleteData)
        }

        fun getItemDetails() = object :
            ItemDetailsLookup.ItemDetails<T>() {
            override fun getPosition() = bindingAdapterPosition
            override fun getSelectionKey(): T = listItems[position]
        }
    }

    public override fun getItem(position: Int) = listItems[position]
    fun getPosition(key: T): Int? = listItems.indexOfFirst { it == key }
}

interface DataItemBindingInterface<T, U: ViewDataBinding> {
    fun bind(item: T, binder: U, setViewData: (id: Long) -> Unit, deleteData: (toDelete: T) -> Unit)
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

class GenericClickListener<T : Any>(private val clickListener: (T) -> Unit) {
    fun onClick(data: T) = clickListener(data)
}

class GenericItemKeyProvider<T, U: ViewDataBinding>(private val adapter: DataItemAdapter<T,U>) :
    ItemKeyProvider<T>(SCOPE_CACHED) {
    override fun getKey(position: Int): T? {
        return adapter.getItem(position)
    }

    override fun getPosition(key: T): Int {
        return adapter.getPosition(key) ?: RecyclerView.NO_POSITION
    }
}

class GenericItemLookUp<T>(private val recyclerView: RecyclerView) : ItemDetailsLookup<T>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<T>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as DataItemAdapter.DataItemViewHolder<T>).getItemDetails()
        }
        return null
    }
}