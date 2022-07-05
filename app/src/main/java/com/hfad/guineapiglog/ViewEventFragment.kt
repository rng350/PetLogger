package com.hfad.guineapiglog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.guineapiglog.databinding.FragmentViewEventBinding

class ViewEventFragment : Fragment() {
    private var _binding: FragmentViewEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewEventBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val eventDao = PetLoggerDatabase.getInstance(application).eventDao

        val eventId = ViewEventFragmentArgs.fromBundle(requireArguments()).eventId

        val viewModelFactory = ViewEventViewModelFactory(eventDao, eventId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ViewEventViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val petAdapter = PetItemAdapter(
            setViewPet = {petID -> viewModel.petNavigator.navigateTo(petID)},
            deletePet = { TODO("Disassociate pet from event") }
        )
        binding.petsList.adapter = petAdapter
        viewModel.petsAssociated.observe(viewLifecycleOwner, Observer {
            it?.let {
                petAdapter.data = it
            }
        })

        viewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = ViewEventFragmentDirections.actionViewEventFragmentToViewPetFragment(it)
                this.findNavController().navigate(action)
                viewModel.petNavigator.onNavigated()
            }
        })


        binding.backButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_viewEventFragment_to_homeFragment)
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}