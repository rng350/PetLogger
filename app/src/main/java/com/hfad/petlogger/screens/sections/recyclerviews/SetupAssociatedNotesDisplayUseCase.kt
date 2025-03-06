package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.databinding.NoteItemBinding
import com.hfad.petlogger.notes.data.Note
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedNotesDisplayUseCase(private val notes: StateFlow<List<Note>>,
                                         private val noteNavigator: Navigator,
                                         private val recyclerView: RecyclerView,
                                         private val lifecycleScope: LifecycleCoroutineScope,
                                         private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Note, NoteItemBinding>(
            layoutId = R.layout.note_item,
            bindingInterface = createEventItemBindingInterface()
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notes.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun createEventItemBindingInterface()
            = object : DataItemBindingInterface<Note, NoteItemBinding> {
        override fun bind(
            item: Note,
            binder: NoteItemBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.note = item
            binder.noteCard.setOnClickListener { null }
            binder.noteCard.setOnClickListener { noteNavigator.navigateTo(item.id) }
        }
    }
}