package com.hfad.petlogger.screens.sections.associatedentities

import RecyclerViewPaginator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentAssociatedPetWeightsDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedPetWeightsUseCase

class AssociatedPetWeightsDisplayFragment : Fragment() {
    private var _binding: FragmentAssociatedPetWeightsDisplayBinding? = null
    val binding: FragmentAssociatedPetWeightsDisplayBinding get() = _binding!!
    private val associatedPetWeightsDisplayViewModel: AssociatedPetWeightsDisplayViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedPetWeightsDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.associatedPetWeightsDisplayViewModel = associatedPetWeightsDisplayViewModel

        SetupAssociatedPetWeightsUseCase(
            associatedPetWeightsDisplayViewModel.weights,
            associatedPetWeightsDisplayViewModel.weightNavigator,
            binding.weightsList,
            lifecycleScope,
            viewLifecycleOwner
        )()

        RecyclerViewPaginator(
            recyclerView = binding.weightsList,
            isLoading = {associatedPetWeightsDisplayViewModel.isLoading()},
            loadMore = {associatedPetWeightsDisplayViewModel.load()},
            onLast = {associatedPetWeightsDisplayViewModel.onLastPage()}
        )

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.weightsList.adapter = null
        _binding = null
    }
}