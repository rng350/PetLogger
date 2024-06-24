package com.hfad.petlogger.recyclerviews

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.CheckableWeightWNameItemBinding
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.selectiontracker.EditSelectionTracker

class SetupWeightMultiPickerUseCase(private val weightList: MutableLiveData<List<CheckableItem<WeightWithPetName>>>,
                                    private val selection: MutableLiveData<List<CheckableItem<WeightWithPetName>>>,
                                    private val selectionTracker: EditSelectionTracker<WeightWithPetName>,
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
                val mutableList = selection.value?.toMutableList() ?: mutableListOf<CheckableItem<WeightWithPetName>>()

                selectionTracker.toggle(item)

                if (mutableList.contains(item)) {
                    mutableList.remove(item)
                    item.isChecked.value = false
                    binder.weightCard.isChecked = false
                } else {
                    mutableList.add(item)
                    item.isChecked.value = true
                    binder.weightCard.isChecked = true
                }
                selection.value = mutableList.toList()
            }

            val observer = Observer<List<CheckableItem<WeightWithPetName>>> {
                binder.weightCard.isChecked = selection.value?.contains(item) ?: false
            }
            selection.observe(lifecycleOwner, observer)
        }
    }
}