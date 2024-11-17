package com.hfad.petlogger.screens.sections.associatedentities

import RecyclerViewPaginator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentAssociatedEventsDisplayBinding
import com.hfad.petlogger.databinding.FragmentEventListBinding
import com.hfad.petlogger.screens.event.EventListViewModel
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedEventsDisplayUseCase

class AssociatedEventsDisplayFragment : Fragment() {
    private var _binding: FragmentEventListBinding? = null
    val binding: FragmentEventListBinding get() = _binding!!
    private val eventListViewModel: EventListViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = eventListViewModel

        SetupAssociatedEventsDisplayUseCase(
            eventListViewModel.event,
            eventListViewModel.eventNavigator,
            binding.eventsList,
            lifecycleScope,
            viewLifecycleOwner
        )()

        RecyclerViewPaginator(
            recyclerView = binding.eventsList,
            isLoading = {eventListViewModel.isLoading()},
            loadMore = {eventListViewModel.load()},
            onLast = {eventListViewModel.onLastPage()}
        )

        binding.addEventButton.setOnClickListener {
            eventListViewModel.newEventNavigator.navigateToNewEntityScreen()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.eventsList.adapter = null
        _binding = null
    }
}