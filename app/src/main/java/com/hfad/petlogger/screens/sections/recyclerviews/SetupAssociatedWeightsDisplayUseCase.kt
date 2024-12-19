package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.WeightItemBinding
import com.hfad.petlogger.weights.WeightForList
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.util.Navigator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedWeightsDisplayUseCase(
    private val weights: StateFlow<List<WeightForList>>,
    private val weightNavigator: Navigator,
    private val recyclerView: RecyclerView,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val weightAdapter = GenericRecyclerViewAdapter<WeightForList, WeightItemBinding>(
        layoutId = R.layout.weight_item,
        bindingInterface = createWeightWithPetNameItemBindingInterface(weightNavigator)
        )
        recyclerView.adapter = weightAdapter
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                weights.collectLatest {
                    weightAdapter.submitList(it)
                }
            }
        }
    }

    private fun createWeightWithPetNameItemBindingInterface(weightNavigator: Navigator)
            = object : DataItemBindingInterface<WeightForList, WeightItemBinding> {
        override fun bind(
            item: WeightForList,
            binder: WeightItemBinding
        ) {
            binder.weight = item

            binder.weightCard.setOnClickListener {
                null
            }
            binder.weightCard.setOnClickListener {
                weightNavigator.navigateTo(item.weightId)
            }
        }
    }
}