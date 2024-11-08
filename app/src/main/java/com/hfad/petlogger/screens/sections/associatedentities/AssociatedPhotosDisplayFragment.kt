package com.hfad.petlogger.screens.sections.associatedentities

import RecyclerViewPaginator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentAssociatedPhotosDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedPhotosDisplayUseCase

class AssociatedPhotosDisplayFragment : Fragment() {
    private var _binding: FragmentAssociatedPhotosDisplayBinding? = null
    val binding: FragmentAssociatedPhotosDisplayBinding get() = _binding!!
    private val assocPhotosViewModel: AssociatedPhotosDisplayViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedPhotosDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.associatedPhotosDisplayViewModel = assocPhotosViewModel

        SetupAssociatedPhotosDisplayUseCase(
            assocPhotosViewModel.photos,
            assocPhotosViewModel.navigator,
            binding.photoList,
            requireContext(),
            lifecycleScope,
            viewLifecycleOwner
        )()

        RecyclerViewPaginator(
            recyclerView = binding.photoList,
            isLoading = {assocPhotosViewModel.isLoading()},
            loadMore = {assocPhotosViewModel.load()},
            onLast = {assocPhotosViewModel.onLastPage()}
        )

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.photoList.adapter = null
        _binding = null
    }
}