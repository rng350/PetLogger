package com.hfad.petlogger.recyclerviews

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.StateFlow

class SetupAssociatedWeightsDisplayUseCase(
    private val weights: StateFlow<List<Weight>>,
    private val weightNavigator: Navigator,
    private val recyclerView: RecyclerView,
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val lifecycleOwner: LifecycleOwner
) {
    operator fun invoke() {

    }
}