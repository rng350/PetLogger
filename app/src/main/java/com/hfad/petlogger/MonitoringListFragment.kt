package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentMonitoringListBinding
import com.hfad.petlogger.photodisplay.stateful.GetAllWeightsWithPetInfoForDisplayUseCase
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator
import com.hfad.petlogger.recyclerviews.SetupAssociatedWeightsDisplayUseCase
import com.hfad.petlogger.repositories.WeightRepository

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
        val getWeightsUseCase = GetAllWeightsWithPetInfoForDisplayUseCase(weightRepository)
        viewModel = ViewModelProvider(this, MonitoringListViewModel.provideFactory(getWeightsUseCase)).get(MonitoringListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setAppBarTitle(getString(R.string.weight_list_header))

        SetupAssociatedWeightsDisplayUseCase(
            weights = viewModel.weights,
            weightNavigator = viewModel.weightNavigator,
            recyclerView = binding.weightsList,
            context = application.applicationContext,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        ).invoke()

        viewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = MonitoringListFragmentDirections.actionMonitoringListFragmentToViewWeightFragment(it)
                this.findNavController().navigateSafe(action)
                viewModel.weightNavigator.onNavigated()
            }
        })

        binding.addWeightButton.setOnClickListener{
            this.findNavController().navigateSafe(MonitoringListFragmentDirections.actionMonitoringListFragmentToNewWeightFragment(""))
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.weightsList.adapter = null
        _binding = null
    }
}