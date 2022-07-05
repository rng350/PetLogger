package com.hfad.guineapiglog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.hfad.guineapiglog.databinding.FragmentViewPetBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

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

        val petId = ViewPetFragmentArgs.fromBundle(requireArguments()).petId

        val viewModelFactory = ViewPetViewModelFactory(petDao, petId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ViewPetViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.pet.observe(viewLifecycleOwner, Observer { it ->
            it?.let {
                viewModel.petDOB.value = viewModel.getBirthDateDisplay()
                viewModel.petAge.value = viewModel.getPetAgeDisplay()
                viewModel.pet?.value?.petProfilePic?.let {
                    Log.i("VIEWPETFRAGMENT", "petprofilepic not null")
                    binding.petPhoto.setImageURI(viewModel.pet?.value?.petProfilePic)
                }
            }
        })



        val eventAdapter = EventItemAdapter(
            setViewEvent = {eventID -> viewModel.eventNavigator.navigateTo(eventID)},
            deleteEvent = { TODO("Disassociate event from pet") }
        )
        binding.eventsList.adapter = eventAdapter
        viewModel.eventsAssociated.observe(viewLifecycleOwner, Observer {
            it?.let {
                eventAdapter.data = it
            }
        })

        viewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = ViewPetFragmentDirections.actionViewPetFragmentToViewEventFragment(it)
                this.findNavController().navigate(action)
                viewModel.eventNavigator.onNavigated()
            }
        })

        val weightAdapter = WeightItemAdapter(
            setViewWeight = {weightID -> viewModel.weightNavigator.navigateTo(weightID)},
            deleteWeight = { TODO("Delete the weight") }
        )
        binding.weightsList.adapter = weightAdapter
        viewModel.weightsAssociated.observe(viewLifecycleOwner, Observer {
            it?.let {
                weightAdapter.data = it.map {WeightWithPetName(it, petDao, viewModel)}.toMutableList()
            }
        })
        viewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                TODO("implement ViewWeightFragment")
            }
        })

        binding.editPetButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_viewPetFragment_to_editPetFragment)
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