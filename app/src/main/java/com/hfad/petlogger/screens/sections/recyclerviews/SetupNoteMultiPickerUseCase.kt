package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.databinding.CheckableNoteShortItemBinding
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter

class SetupNoteMultiPickerUseCase(private val noteList: LiveData<List<CheckableItem<Note>>>,
                                  private val selection: LiveData<List<Note>>,
                                  private val selectionTracker: MultiSelectionTracker<Note>,
                                  private val recyclerView: RecyclerView,
                                  private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<Note>, CheckableNoteShortItemBinding>(
            layoutId = R.layout.checkable_note_short_item,
            bindingInterface = createCheckableNoteItemBindingInterface()
        )
        recyclerView.adapter = adapter

        noteList.observe(lifecycleOwner, Observer {
            adapter.submitList(it.toMutableList())
        })
    }

    private fun createCheckableNoteItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<Note>, CheckableNoteShortItemBinding> {
        override fun bind(item: CheckableItem<Note>, binder: CheckableNoteShortItemBinding) {
            binder.checkableNote = item
            binder.note = item.item

            binder.noteCard.isChecked = item.isChecked.value!!

            binder.noteCard.setOnClickListener { null }
            binder.noteCard.setOnClickListener {
                selectionTracker.toggle(item)
            }

            val observer = Observer<List<Note>> {
                binder.noteCard.isChecked = selection.value?.contains(item.item) ?: false
            }
            selection.observe(lifecycleOwner, observer)
        }
    }
}