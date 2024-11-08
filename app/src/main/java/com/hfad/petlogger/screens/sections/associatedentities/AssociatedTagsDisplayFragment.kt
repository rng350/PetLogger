package com.hfad.petlogger.screens.sections.associatedentities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.hfad.petlogger.databinding.FragmentAssociatedTagsDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedTagsDisplayUseCase

class AssociatedTagsDisplayFragment : Fragment() {
    private var _binding: FragmentAssociatedTagsDisplayBinding? = null
    val binding: FragmentAssociatedTagsDisplayBinding get() = _binding!!
    private val associatedTagsDisplayViewModel: AssociatedTagsDisplayViewModel by viewModels({requireParentFragment().requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedTagsDisplayBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.associatedTagsDisplayViewModel = associatedTagsDisplayViewModel

        binding.tagsList.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        SetupAssociatedTagsDisplayUseCase(
            tags = associatedTagsDisplayViewModel.tags,
            tagNavigator = associatedTagsDisplayViewModel.navigator,
            recyclerView = binding.tagsList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.tagsList.adapter = null
        _binding = null
    }
}