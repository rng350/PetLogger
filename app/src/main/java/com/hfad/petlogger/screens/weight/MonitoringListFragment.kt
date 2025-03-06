package com.hfad.petlogger.screens.weight

import RecyclerViewPaginator
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.databinding.FragmentMonitoringListBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.weights.domain.usecases.GetMoreOfAllWeightsUseCase
import com.hfad.petlogger.weights.domain.WeightRepository
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedWeightsDisplayUseCase
import com.hfad.petlogger.weights.domain.usecases.GetSearchedWeightsForGeneralDisplayUseCase

class MonitoringListFragment : Fragment() {
    private var _binding: FragmentMonitoringListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MonitoringListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonitoringListBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val weightRepository = WeightRepository(database)
        val getWeightsUseCase = GetMoreOfAllWeightsUseCase(weightRepository, weightsAmt = 15)
        val getSearchedWeights = GetSearchedWeightsForGeneralDisplayUseCase(database.weightDao, weightsAmt = 15)
        viewModel = ViewModelProvider(this,
            MonitoringListViewModel.provideFactory(getWeightsUseCase, getSearchedWeights)
        ).get(MonitoringListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupAssociatedWeightsDisplayUseCase(
            weights = viewModel.weights,
            weightNavigator = viewModel.weightNavigator,
            recyclerView = binding.weightsList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

        RecyclerViewPaginator(
            recyclerView = binding.weightsList,
            loadMore = {viewModel.load()},
            isLoading = {viewModel.isLoading()},
            onLast = {viewModel.onLastPage()}
        )

        if (findNavController().previousBackStackEntry == null) {
            binding.weightListTopAppBar.navigationIcon = null
        } else {
            binding.weightListTopAppBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onQueryTextChanged(newText)
                return true
            }
        })

        viewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = MonitoringListFragmentDirections.actionMonitoringListFragmentToViewWeightFragment(it)
                this.findNavController().navigateSafe(action)
                viewModel.weightNavigator.onNavigated()
            }
        })

        binding.addWeightButton.setOnClickListener{
            this.findNavController().navigateSafe(MonitoringListFragmentDirections.actionMonitoringListFragmentToNewWeightFragment())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.weightsList.adapter = null
        _binding = null
    }
}