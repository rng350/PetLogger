package com.hfad.petlogger.screens.pet.petmultiselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.hfad.petlogger.databinding.FragmentPetMultiSelectionSectionDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupPetMultiPickerSelectionDisplayUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PetMultiSelectionSectionDisplayFragment : Fragment() {
    private var _binding: FragmentPetMultiSelectionSectionDisplayBinding? = null
    val binding: FragmentPetMultiSelectionSectionDisplayBinding
        get() = _binding!!

    val viewModel: PetMultiSelectionViewModel by viewModels({ requireParentFragment().requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetMultiSelectionSectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.addPetsButton.setOnClickListener {
            binding.addPetsButton.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                PetMultiSelectionSectionDialogFragment().show(childFragmentManager, "PET_MULTI_PICKER")
                delay(200)
                binding.addPetsButton.isEnabled = true
            }
        }

        binding.resetButton.setOnClickListener {
            viewModel.reset()
        }

        SetupPetMultiPickerSelectionDisplayUseCase(
            selection = viewModel.selectionTracker.visibleCurrentSelection,
            selectionTracker = viewModel.selectionTracker,
            recyclerView = binding.petsList,
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext()
        )()

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onCurrentSelectionDisplayQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onCurrentSelectionDisplayQueryTextChange(newText)
                return true
            }
        })

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