package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.databinding.NoteShortItemBinding
import com.hfad.petlogger.notes.data.Note

class SetupNoteMultiPickerSelectionDisplayUseCase(private val selection: LiveData<List<Note>>,
                                                  private val selectionTracker: MultiSelectionTracker<Note>,
                                                  private val recyclerView: RecyclerView,
                                                  private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Note, NoteShortItemBinding>(
            layoutId = R.layout.note_short_item,
            bindingInterface = createNoteItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createNoteItemBindingInterface() = object:
        DataItemBindingInterface<Note, NoteShortItemBinding> {
        override fun bind(
            item: Note,
            binder: NoteShortItemBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.note = item

            binder.noteCard.setOnClickListener { null }
            binder.noteCard.setOnClickListener {
                selectionTracker.remove(item)
            }
        }
    }
}