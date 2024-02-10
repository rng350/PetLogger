package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.hfad.petlogger.databinding.FragmentViewPetBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator

class ViewPetFragment : Fragment() {

    private var _binding: FragmentViewPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val weightDao = PetLoggerDatabase.getInstance(application).weightDao

        val petId = ViewPetFragmentArgs.fromBundle(requireArguments()).petId

        val viewModelFactory = ViewPetViewModelFactory(petDao, weightDao, petId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ViewPetViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.petProfilePhoto.observe(viewLifecycleOwner, Observer { it ->
            Glide.with(requireContext())
                .load(it.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binding.petPhoto)
        })

        val eventAdapter = BindingInterfaceCreator.setupNavigatableEventAdapter(viewModel.eventNavigator)
        binding.eventsList.adapter = eventAdapter
        viewModel.eventsAssociated.observe(viewLifecycleOwner, Observer {
            it?.let {
                eventAdapter.submitList(it)
            }
        })

        viewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = ViewPetFragmentDirections.actionViewPetFragmentToViewEventFragment(it)
                this.findNavController().navigate(action)
                viewModel.eventNavigator.onNavigated()
            }
        })

        val weightAdapter = BindingInterfaceCreator.setupNavigatableWeightAdapter(viewModel.weightNavigator)
        binding.weightsList.adapter = weightAdapter
        viewModel.weightsAssociated.observe(viewLifecycleOwner, Observer {
            it?.let {
                weightAdapter.submitList(it)
            }
        })
        viewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                TODO("implement ViewWeightFragment")
            }
        })

        binding.editPetButton.setOnClickListener {
            this.findNavController().navigate(ViewPetFragmentDirections.actionViewPetFragmentToEditPetFragment(petId))
        }
        binding.backButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_viewPetFragment_to_homeFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}