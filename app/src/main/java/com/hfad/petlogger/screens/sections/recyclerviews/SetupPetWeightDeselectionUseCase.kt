package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckableWeightItemDeleteBinding
import com.hfad.petlogger.weights.PetWeightForSelection
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiDeselectionDisplay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupPetWeightDeselectionUseCase(
    private val weights: StateFlow<List<CheckableItem<PetWeightForSelection>>>,
    private val recyclerView: RecyclerView,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val lifecycleOwner: LifecycleOwner,
    private val selectionTracker: MultiDeselectionDisplay<PetWeightForSelection>
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<PetWeightForSelection>, CheckableWeightItemDeleteBinding>(
            layoutId = R.layout.checkable_weight_item_delete,
            bindingInterface = createDeletableWeightItemBindingInterface()
        )
        recyclerView.adapter = adapter
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                weights.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun createDeletableWeightItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<PetWeightForSelection>, CheckableWeightItemDeleteBinding> {
        override fun bind(
            item: CheckableItem<PetWeightForSelection>,
            binder: CheckableWeightItemDeleteBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.checkableWeight = item
            binder.weightDisplay = item.item
            binder.weightCard.isChecked = item.isChecked.value!!

            (binder.root.context as? LifecycleOwner)?.let { itemLifecycleOwner ->
                item.isChecked.observe(itemLifecycleOwner, Observer {
                    binder.weightCard.isChecked = it
                })
            }

            binder.weightCard.setOnClickListener { null }
            binder.weightCard.setOnClickListener {
                selectionTracker.toggleItem(item)
            }
        }
    }
}