package com.hfad.petlogger.screens.weight

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetWeightDeselectionBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupPetWeightDeselectionUseCase

class PetWeightDeselectionFragment : Fragment() {
    private var _binding: FragmentPetWeightDeselectionBinding? = null
    val binding: FragmentPetWeightDeselectionBinding get() = _binding!!
    private val petWeightDeselectionViewModel: PetWeightDeselectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetWeightDeselectionBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.petWeightDeselectionViewModel = petWeightDeselectionViewModel

        SetupPetWeightDeselectionUseCase(
            petWeightDeselectionViewModel.weights,
            binding.weightsList,
            viewLifecycleOwner,
            petWeightDeselectionViewModel.selectionTracker
        )()

        binding.resetButton.setOnClickListener {
            petWeightDeselectionViewModel.reset()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.weightsList.adapter = null
        _binding = null
    }
}