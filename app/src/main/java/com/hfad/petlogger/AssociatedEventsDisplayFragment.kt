package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentAssociatedEventsDisplayBinding
import com.hfad.petlogger.recyclerviews.SetupAssociatedEventsDisplayUseCase

class AssociatedEventsDisplayFragment : Fragment() {
    private var _binding: FragmentAssociatedEventsDisplayBinding? = null
    val binding: FragmentAssociatedEventsDisplayBinding get() = _binding!!
    private val associatedEventsDisplayViewModel: AssociatedEventsDisplayViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedEventsDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.associatedEventsDisplayViewModel = associatedEventsDisplayViewModel

        SetupAssociatedEventsDisplayUseCase(
            associatedEventsDisplayViewModel.events,
            associatedEventsDisplayViewModel.eventNavigator,
            binding.eventsList,
            lifecycleScope,
            viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.eventsList.adapter = null
        _binding = null
    }
}