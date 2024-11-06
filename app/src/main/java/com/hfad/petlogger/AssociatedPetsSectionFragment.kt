package com.hfad.petlogger

import RecyclerViewPaginator
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentAssociatedPetsSectionBinding
import com.hfad.petlogger.recyclerviews.SetupAssociatedPetsDisplayUseCase

class AssociatedPetsSectionFragment : Fragment() {
    private var _binding: FragmentAssociatedPetsSectionBinding? = null
    val binding: FragmentAssociatedPetsSectionBinding get() = _binding!!

    private val associatedPetsDisplayViewModel: AssociatedPetsDisplayViewModel by viewModels({requireParentFragment().requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedPetsSectionBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        SetupAssociatedPetsDisplayUseCase(
            associatedPetsDisplayViewModel.pets,
            associatedPetsDisplayViewModel.navigator,
            binding.petsList,
            requireContext(),
            lifecycleScope,
            viewLifecycleOwner
        )()

        RecyclerViewPaginator(
            recyclerView = binding.petsList,
            isLoading = { associatedPetsDisplayViewModel.isLoading() },
            loadMore = { associatedPetsDisplayViewModel.load() },
            onLast = { associatedPetsDisplayViewModel.onLastPage() }
        )

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}