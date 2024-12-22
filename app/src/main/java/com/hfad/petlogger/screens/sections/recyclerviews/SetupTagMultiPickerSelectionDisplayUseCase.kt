package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedTagBinding
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker

class SetupTagMultiPickerSelectionDisplayUseCase(
    private val selection: LiveData<List<Tag>>,
    private val selectionTracker: MultiSelectionTracker<Tag>,
    private val recyclerView: RecyclerView,
    private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Tag, ItemSelectedTagBinding>(
            layoutId = R.layout.item_selected_tag,
            bindingInterface = createTagItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createTagItemBindingInterface() = object :
        DataItemBindingInterface<Tag, ItemSelectedTagBinding> {
        override fun bind(
            item: Tag,
            binder: ItemSelectedTagBinding,
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