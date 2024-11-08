package com.hfad.petlogger.screens.pet.petmultiselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetMultiSelectionDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupPetMultiPickerSelectionDisplayUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PetMultiSelectionDisplayFragment : Fragment() {
    private var _binding: FragmentPetMultiSelectionDisplayBinding? = null
    val binding: FragmentPetMultiSelectionDisplayBinding
        get() = _binding!!

    val viewModel: PetMultiSelectionViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetMultiSelectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.addPetsButton.setOnClickListener {
            binding.addPetsButton.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                PetMultiSelectionDialogFragment().show(childFragmentManager, "PET_MULTI_PICKER")
                delay(200)
                binding.addPetsButton.isEnabled = true
            }
        }

        binding.resetButton.setOnClickListener {
            viewModel.reset()
        }

        SetupPetMultiPickerSelectionDisplayUseCase(
            selection = viewModel.selectionTracker.currentSelection,
            selectionTracker = viewModel.selectionTracker,
            recyclerView = binding.petsList,
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext()
        )()

        return view
    }


    override fun onStop() {
        super.onStop()
        binding.addPetsButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}