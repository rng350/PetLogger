package com.hfad.petlogger.screens.sections.associatedentities

import RecyclerViewPaginator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentMonitoringListBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedWeightsDisplayUseCase
import com.hfad.petlogger.screens.weight.MonitoringListViewModel

class AssociatedWeightsForGeneralDisplayFragment : Fragment() {
    private var _binding: FragmentMonitoringListBinding? = null
    val binding: FragmentMonitoringListBinding get() = _binding!!
    private val monitoringListViewModel: MonitoringListViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonitoringListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = monitoringListViewModel

        binding.addWeightButton.setOnClickListener {
            monitoringListViewModel.newWeightNavigator.navigateToNewEntityScreen()
        }

        SetupAssociatedWeightsDisplayUseCase(
            weights = monitoringListViewModel.weights,
            weightNavigator = monitoringListViewModel.weightNavigator,
            recyclerView = binding.weightsList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

        RecyclerViewPaginator(
            recyclerView = binding.weightsList,
            loadMore = {monitoringListViewModel.load()},
            isLoading = {monitoringListViewModel.isLoading()},
            onLast = {monitoringListViewModel.onLastPage()}
        )

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                monitoringListViewModel.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                monitoringListViewModel.onQueryTextChanged(newText)
                return true
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.weightsList.adapter = null
        _binding = null
    }
}