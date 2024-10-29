package com.hfad.petlogger.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.ItemSelectedWeightBinding
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.selectiontracker.MultiSelectionTracker

class SetupWeightMultiPickerSelectionDisplayUseCase(private val selection: LiveData<List<WeightWithPetName>>,
                                                    private val selectionTracker: MultiSelectionTracker<WeightWithPetName>,
                                                    private val recyclerView: RecyclerView,
                                                    private val lifecycleOwner: LifecycleOwner
) {

    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<WeightWithPetName, ItemSelectedWeightBinding>(
            layoutId = R.layout.item_selected_weight,
            bindingInterface = createCheckableWeightItemBindingInterface()
        )
        recyclerView.adapter = adapter

        selection.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun createCheckableWeightItemBindingInterface() = object:
        DataItemBindingInterface<WeightWithPetName, ItemSelectedWeightBinding> {
        override fun bind(item: WeightWithPetName, binder: ItemSelectedWeightBinding) {
            binder.weight = item
            binder.weightCard.setOnClickListener { null }
            binder.weightCard.setOnClickListener {
                selectionTracker.remove(item)
            }
        }
    }
}