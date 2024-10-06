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
import androidx.lifecycle.repeatOnLifecycle
import com.hfad.petlogger.databinding.FragmentAssociatedPetsDisplayBinding
import com.hfad.petlogger.recyclerviews.SetupAssociatedPetsDisplayUseCase
import kotlinx.coroutines.launch

class AssociatedPetsDisplayFragment : Fragment() {
    private var _binding: FragmentAssociatedPetsDisplayBinding? = null
    val binding: FragmentAssociatedPetsDisplayBinding get() = _binding!!

    private val associatedPetsDisplayViewModel: AssociatedPetsDisplayViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedPetsDisplayBinding.inflate(inflater, container, false)
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