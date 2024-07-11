package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentHomeBinding
import androidx.lifecycle.Observer
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val eventDao = PetLoggerDatabase.getInstance(application).eventDao
        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val weightDao = PetLoggerDatabase.getInstance(application).weightDao

        val viewModelFactory = HomeViewModelFactory(petDao, eventDao, weightDao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        
        binding.addPetButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newPetFragment)
        }
        viewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = HomeFragmentDirections.actionHomeFragmentToViewPetFragment(it)
                this.findNavController().navigate(action)
                viewModel.petNavigator.onNavigated()
            }
        })

        binding.addEventButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newEventFragment)
        }
        viewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = HomeFragmentDirections.actionHomeFragmentToViewEventFragment(it)
                this.findNavController().navigate(action)
                viewModel.eventNavigator.onNavigated()
            }
        })

        binding.addWeightButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newWeightFragment)
        }
        Log.d("Home","Created")

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}