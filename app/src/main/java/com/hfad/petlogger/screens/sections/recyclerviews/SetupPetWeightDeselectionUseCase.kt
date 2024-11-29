package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckableWeightItemDeleteBinding
import com.hfad.petlogger.weights.PetWeightForSelection
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.SelectionTracker

class SetupPetWeightDeselectionUseCase(
    private val weights: LiveData<List<CheckableItem<PetWeightForSelection>>>,
    private val recyclerView: RecyclerView,
    private val lifecycleOwner: LifecycleOwner,
    private val selectionTracker: SelectionTracker<PetWeightForSelection>
) {
    private val activeObservers = HashMap<CheckableWeightItemDeleteBinding, Observer<List<CheckableItem<PetWeightForSelection>>>>()
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<PetWeightForSelection>, CheckableWeightItemDeleteBinding>(
            layoutId = R.layout.checkable_weight_item_delete,
            bindingInterface = createDeletableWeightItemBindingInterface()
        )
        recyclerView.adapter = adapter
        weights.observe(lifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun createDeletableWeightItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<PetWeightForSelection>, CheckableWeightItemDeleteBinding> {
        override fun bind(
            item: CheckableItem<PetWeightForSelection>,
            binder: CheckableWeightItemDeleteBinding
        ) {
            binder.checkableWeight = item
            binder.weightDisplay = item.item
            binder.weightCard.isChecked = item.isChecked.value!!

            // remove old observer
            val fetchedActiveObserver = activeObservers[binder]
            fetchedActiveObserver?.let {
                selectionTracker.selectionToAdd.removeObserver(it)
                activeObservers.remove(binder)
            }
            // add new observer
            val observer = Observer<List<CheckableItem<PetWeightForSelection>>> {
                binder.weightCard.isChecked = it.contains(item)
            }
            selectionTracker.selectionToAdd.observe(lifecycleOwner, observer)
            activeObservers[binder] = observer

            binder.weightCard.setOnClickListener { null }
            binder.weightCard.setOnClickListener {
                selectionTracker.toggle(item)
            }
        }
    }
}