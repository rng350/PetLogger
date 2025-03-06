package com.hfad.petlogger.screens.sections.recyclerviews

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.R
import com.hfad.petlogger.common.recyclerviews.DataItemBindingInterface
import com.hfad.petlogger.common.recyclerviews.GenericRecyclerViewAdapter
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.databinding.PetItemCellBinding
import com.hfad.petlogger.pets.data.PetWithProfilePic
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupAssociatedPetsDisplayUseCase(private val pets: StateFlow<List<PetWithProfilePic>>,
                                        private val petNavigator: Navigator,
                                        private val recyclerView: RecyclerView,
                                        private val context: Context,
                                        private val lifecycleScope: LifecycleCoroutineScope,
                                        private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {
        val adapter = GenericRecyclerViewAdapter<PetWithProfilePic, PetItemCellBinding>(
            layoutId = R.layout.pet_item_cell,
            bindingInterface = createPetsAssociatedDisplayItemBindingInterface()
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pets.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun createPetsAssociatedDisplayItemBindingInterface() = object:
        DataItemBindingInterface<PetWithProfilePic, PetItemCellBinding> {
        override fun bind(
            item: PetWithProfilePic,
            binder: PetItemCellBinding,
            itemLifecycleOwner: LifecycleOwner
        ) {
            binder.pet = item
            Glide.with(context).clear(binder.petProfileImage)
            item.petProfilePicUri?.let {
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            }

            binder.petCard.setOnClickListener { null }
            binder.petCard.setOnClickListener {
                petNavigator.navigateTo(item.petId)
            }
        }
    }
}