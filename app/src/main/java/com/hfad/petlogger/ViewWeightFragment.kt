package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.databinding.FragmentViewWeightBinding


class ViewWeightFragment : Fragment() {
    private var _binding: FragmentViewWeightBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewWeightBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireActivity().application

        val weightDao = PetLoggerDatabase.getInstance(application).weightDao
        val weightId = ViewWeightFragmentArgs.fromBundle(requireArguments()).weightId

        val viewModelFactory = ViewWeightViewModelFactory(weightDao, weightId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ViewWeightViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return view
    }
}