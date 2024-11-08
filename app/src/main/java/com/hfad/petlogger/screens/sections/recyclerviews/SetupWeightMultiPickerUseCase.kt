package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckableWeightWNameItemBinding
import com.hfad.petlogger.weights.WeightWithPetName
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker

class SetupWeightMultiPickerUseCase(private val weightList: LiveData<List<CheckableItem<WeightWithPetName>>>,
                                    private val selection: LiveData<List<WeightWithPetName>>,
                                    private val selectionTracker: MultiSelectionTracker<WeightWithPetName>,
                                    private val recyclerView: RecyclerView,
                                    private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<CheckableItem<WeightWithPetName>, CheckableWeightWNameItemBinding>(
            layoutId = R.layout.checkable_weight_w_name_item,
            bindingInterface = createCheckableWeightItemBindingInterface()
        )
        recyclerView.adapter = adapter

        weightList.observe(lifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
    private fun createCheckableWeightItemBindingInterface() = object:
        DataItemBindingInterface<CheckableItem<WeightWithPetName>, CheckableWeightWNameItemBinding> {
        override fun bind(item: CheckableItem<WeightWithPetName>, binder: CheckableWeightWNameItemBinding) {
            binder.weight = item.item

            binder.weightCard.setOnClickListener { null }
            binder.weightCard.setOnClickListener {
                selectionTracker.toggle(item)
            }

            val observer = Observer<List<WeightWithPetName>> {
                binder.weightCard.isChecked = selection.value?.contains(item.item) ?: false
            }
            selection.observe(lifecycleOwner, observer)
        }
    }
}