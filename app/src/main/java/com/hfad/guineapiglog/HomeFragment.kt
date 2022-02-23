package com.hfad.guineapiglog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.guineapiglog.databinding.FragmentHomeBinding
import androidx.lifecycle.Observer

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

        val viewModelFactory = HomeViewModelFactory(petDao, eventDao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = PetItemAdapter(petDao)
        binding.petsList.adapter = adapter

        binding.addPetButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newPetFragment)
        }

        binding.addEventButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newEventFragment)
        }

        viewModel.pets.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}