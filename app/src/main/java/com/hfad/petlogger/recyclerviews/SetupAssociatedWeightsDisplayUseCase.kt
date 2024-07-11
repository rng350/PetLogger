package com.hfad.petlogger.recyclerviews

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.WeightItemBinding
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightForList
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedWeightsDisplayUseCase(
    private val weights: StateFlow<List<WeightForList>>,
    private val weightNavigator: Navigator,
    private val recyclerView: RecyclerView,
    private val context: Context,
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

            Glide.with(context).clear(binder.petProfileImage)
            item.let {
                Glide.with(context)
                    .load(it.weightPetPhotoUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            }

            binder.weightCard.setOnClickListener {
                null
            }
            binder.weightCard.setOnClickListener {
                weightNavigator.navigateTo(item.weightId)
            }
        }
    }
}