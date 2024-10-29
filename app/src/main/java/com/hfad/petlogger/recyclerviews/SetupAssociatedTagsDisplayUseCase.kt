package com.hfad.petlogger.recyclerviews

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedTagBinding
import com.hfad.petlogger.databinding.ItemTagBinding
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedTagsDisplayUseCase(private val tags: StateFlow<List<Tag>>,
                                        private val tagNavigator: Navigator,
                                        private val recyclerView: RecyclerView,
                                        private val lifecycleScope: LifecycleCoroutineScope,
                                        private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<Tag, ItemTagBinding>(
            layoutId = R.layout.item_tag,
            bindingInterface = createTagItemBindingInterface()
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tags.collectLatest {
                    Log.d("AssocTags", "Tags: $it")
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun createTagItemBindingInterface() = object :
        DataItemBindingInterface<Tag, ItemTagBinding> {
        override fun bind(item: Tag, binder: ItemTagBinding) {
            binder.tag = item

            binder.tagChip.setOnClickListener { null }
            binder.tagChip.setOnClickListener {
                tagNavigator.navigateTo(item.tagId)
            }
        }
    }
}