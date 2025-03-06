package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.databinding.ItemTagBinding
import com.hfad.petlogger.tags.data.Tag

class SetupTagMultiPickerSelectionDisplayUseCase(
    private val selection: LiveData<List<Tag>>,
    private val selectionTracker: MultiSelectionTracker<Tag>,
    private val recyclerView: RecyclerView,
    private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Tag, ItemTagBinding>(
            layoutId = R.layout.item_tag,
            bindingInterface = createTagItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createTagItemBindingInterface() = object :
        DataItemBindingInterface<Tag, ItemTagBinding> {
        override fun bind(
            item: Tag,
            binder: ItemTagBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.tag = item

            binder.tagChip.setOnClickListener { null }
            binder.tagChip.setOnClickListener {
                selectionTracker.remove(item)
            }
        }
    }
}