package com.hfad.guineapiglog

import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.guineapiglog.databinding.FragmentHomeBinding
import androidx.lifecycle.Observer
import com.hfad.guineapiglog.databinding.EventItemBinding
import com.hfad.guineapiglog.databinding.PetItemBinding
import com.hfad.guineapiglog.databinding.WeightItemBinding

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

        val petAdapter = BindingInterfaceCreator.setupNavigatablePetAdapter(viewModel.petNavigator)
        binding.petsList.adapter = petAdapter
        binding.addPetButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newPetFragment)
        }
        viewModel.pets.observe(viewLifecycleOwner, Observer {
            it?.let {
                petAdapter.submitList(it)
            }
        })
        viewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = HomeFragmentDirections.actionHomeFragmentToViewPetFragment(it)
                this.findNavController().navigate(action)
                viewModel.petNavigator.onNavigated()
            }
        })

        val eventAdapter = BindingInterfaceCreator.setupNavigatableEventAdapter(viewModel.eventNavigator)
        binding.eventsList.adapter = eventAdapter
        binding.addEventButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newEventFragment)
        }
        viewModel.events.observe(viewLifecycleOwner, Observer {
            it?.let {
                eventAdapter.submitList(it)
            }
        })
        viewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = HomeFragmentDirections.actionHomeFragmentToViewEventFragment(it)
                this.findNavController().navigate(action)
                viewModel.eventNavigator.onNavigated()
            }
        })

        val weightAdapter = BindingInterfaceCreator.setupNavigatableWeightWithPetNameAdapter(viewModel.weightNavigator)
        viewModel.weights.observe(viewLifecycleOwner, Observer {
            it?.let {
                weightAdapter.submitList(it)
            }
        })
        binding.weightsList.adapter = weightAdapter
        binding.addWeightButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_homeFragment_to_newWeightFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*private fun createPetItemBindingInterface() = object : DataItemBindingInterface<Pet, PetItemBinding> {
        override fun bind(
            item: Pet,
            binder: PetItemBinding
        ) {
            binder.pet = item
            binder.viewPetButton.setOnClickListener {
                binding.viewModel.petNavigator.navigateTo(item.petID)
            }
            binder.deletePetButton.setOnClickListener {
            }
        }
    }

    private fun createEventItemBindingInterface()
        = object : DataItemBindingInterface<Event, EventItemBinding> {
        override fun bind(
            item: Event,
            binder: EventItemBinding
        ) {
            binder.event = item
            binder.viewEventButton.setOnClickListener {
                binding.viewModel.eventNavigator.navigateTo(item.eventId)
            }
            binder.deleteEventButton.setOnClickListener {
            }
        }
    }

    private fun createWeightItemBindingInterface()
        = object : DataItemBindingInterface<WeightWithPetName, WeightItemBinding> {
        override fun bind(
            item: WeightWithPetName,
            binder: WeightItemBinding
        ) {
            binder.weight = item
            binder.viewWeightButton.setOnClickListener {
                binding.viewModel.weightNavigator.navigateTo(item.weight.id)
            }
            binder.deleteWeightButton.setOnClickListener {
            }
        }
    }*/
}