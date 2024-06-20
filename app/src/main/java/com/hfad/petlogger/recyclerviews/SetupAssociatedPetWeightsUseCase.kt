package com.hfad.petlogger.recyclerviews

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.PetWeightItemBinding
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedPetWeightsUseCase(private val weights: StateFlow<List<Weight>>,
                                       private val weightNavigator: Navigator,
                                       private val recyclerView: RecyclerView,
                                       private val lifecycleScope: LifecycleCoroutineScope,
                                       private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter =  GenericRecyclerViewAdapter<Weight, PetWeightItemBinding>(
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
            = object : DataItemBindingInterface<Weight, PetWeightItemBinding> {
        override fun bind(
            item: Weight,
            binder: PetWeightItemBinding
        ) {
            binder.weight = item
            binder.card.setOnClickListener { null }
            binder.card.setOnClickListener {
                weightNavigator.navigateTo(item.id)
            }
        }
    }
}