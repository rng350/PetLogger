package com.hfad.guineapiglog

import android.os.Bundle
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

        viewModel.pet.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.petDOB.value = viewModel.getBirthDateDisplay()
                viewModel.petAge.value = viewModel.getPetAgeDisplay()
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