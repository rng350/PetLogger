package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentMonitoringListBinding
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator

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
        val weightDao = PetLoggerDatabase.getInstance(application).weightDao
        val petDao = PetLoggerDatabase.getInstance(application).petDao

        val viewModelFactory = MonitoringListViewModelFactory(weightDao, petDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MonitoringListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        /** BindingInterfaceCreator.setupGalleryPhotoItemAdapter(
        viewModel.photos,
        binding.gallery,
        viewLifecycleOwner,
        requireContext(),
        viewModel.photoNavigator)

        binding.addPhotoButton.setOnClickListener {
        this.findNavController().navigate(R.id.action_petListFragment_to_newPetFragment)
        }**/

        val mainActivity = (activity as MainActivity)
        mainActivity.setTopAppBarTitle(getString(R.string.weight_list_header))
        mainActivity.disableTopAppBarSubtitle()

        val weightAdapter = BindingInterfaceCreator.setupNavigatableWeightWithPetNameAdapter(viewModel.weightNavigator)
        binding.weightsList.adapter = weightAdapter
        viewModel.weights.observe(viewLifecycleOwner, Observer {
            it?.let {
                weightAdapter.submitList(it)
            }
        })
        viewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = MonitoringListFragmentDirections.actionMonitoringListFragmentToViewWeightFragment(it)
                this.findNavController().navigate(action)
                viewModel.weightNavigator.onNavigated()
            }
        })

        binding.addWeightButton.setOnClickListener{
            this.findNavController().navigate(MonitoringListFragmentDirections.actionMonitoringListFragmentToNewWeightFragment())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}