package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckableTagItemBinding
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker

class SetupTagMultiPickerUseCase(private val tagList: LiveData<List<CheckableItem<Tag>>>,
                                 private val selection: LiveData<List<Tag>>,
                                 private val selectionTracker: MultiSelectionTracker<Tag>,
                                 private val recyclerView: RecyclerView,
                                 private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Tag>, CheckableTagItemBinding>(
            layoutId = R.layout.checkable_tag_item,
            bindingInterface = createCheckableTagItemBindingInterface()
        )
        recyclerView.adapter = adapter

        tagList.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckableTagItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<Tag>, CheckableTagItemBinding> {
        override fun bind(item: CheckableItem<Tag>, binder: CheckableTagItemBinding) {
            binder.tag = item.item
            binder.tagChip.setOnClickListener { null }
            binder.tagChip.setOnClickListener {
                selectionTracker.toggle(item)
            }
            val observer = Observer<List<Tag>> {
                binder.tagChip.isChecked = selection.value?.contains(item.item) ?: false
            }
            selection.observe(lifecycleOwner, observer)
        }
    }
}