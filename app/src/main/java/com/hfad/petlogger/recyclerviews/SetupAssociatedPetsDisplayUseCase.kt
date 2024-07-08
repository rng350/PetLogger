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
import com.hfad.petlogger.databinding.PetItemBinding
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.util.Navigator
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
        val adapter = GenericRecyclerViewAdapter<PetWithProfilePic, PetItemBinding>(
            layoutId = R.layout.pet_item,
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
        DataItemBindingInterface<PetWithProfilePic, PetItemBinding> {
        override fun bind(
            item: PetWithProfilePic,
            binder: PetItemBinding
        ) {
            binder.pet = item
            Glide.with(context).clear(binder.petProfileImage)
            item.profilePic?.let {
                Glide.with(context)
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binder.petProfileImage)
            }

            binder.petCard.setOnClickListener { null }
            binder.petCard.setOnClickListener {
                petNavigator.navigateTo(item.pet.petID)
            }
        }
    }
}