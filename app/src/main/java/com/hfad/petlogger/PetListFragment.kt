package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentPetListBinding
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator

class PetListFragment : Fragment() {
    private var _binding: FragmentPetListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PetListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetListBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val petDao = PetLoggerDatabase.getInstance(application).petDao

        val viewModelFactory = PetListViewModelFactory(petDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PetListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        BindingInterfaceCreator.setupPetWithProfilePhotoAdapter(
            viewModel.pets,
            binding.petsList,
            viewLifecycleOwner,
            requireContext(),
            viewModel.petNavigator)

        binding.addPetButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_petListFragment_to_newPetFragment)
        }
        viewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = PetListFragmentDirections.actionPetListFragmentToViewPetFragment(it)
                this.findNavController().navigate(action)
                viewModel.petNavigator.onNavigated()
            }
        })
        
        val mainActivity = (activity as MainActivity)
        mainActivity.setTopAppBarTitle("Pet List")
        mainActivity.disableTopAppBarSubtitle()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}