package com.hfad.petlogger.screens.sections.recyclerviews

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.PetWeightItemBinding
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.weights.PetWeightForDisplay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedPetWeightsUseCase(private val weights: StateFlow<List<PetWeightForDisplay>>,
                                       private val weightNavigator: Navigator,
                                       private val recyclerView: RecyclerView,
                                       private val lifecycleScope: LifecycleCoroutineScope,
                                       private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter =  GenericRecyclerViewAdapter<PetWeightForDisplay, PetWeightItemBinding>(
            layoutId = R.layout.pet_weight_item,
            bindingInterface = createWeightItemBindingInterface()
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

    private fun createWeightItemBindingInterface()
            = object : DataItemBindingInterface<PetWeightForDisplay, PetWeightItemBinding> {
        override fun bind(
            item: PetWeightForDisplay,
            binder: PetWeightItemBinding
        ) {
            binder.weight = item
            binder.weightCard.setOnClickListener { null }
            binder.weightCard.setOnClickListener {
                weightNavigator.navigateTo(item.weightId)
            }
        }
    }
}