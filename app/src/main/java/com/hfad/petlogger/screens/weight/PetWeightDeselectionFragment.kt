package com.hfad.petlogger.screens.weight

import RecyclerViewPaginator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hfad.petlogger.R
import com.hfad.petlogger.common.selectiontracker.MultiDeselectionTracker
import com.hfad.petlogger.databinding.FragmentPetWeightDeselectionBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupPetWeightDeselectionUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
            petWeightDeselectionViewModel.weightDisplay,
            binding.weightsList,
            lifecycleScope,
            viewLifecycleOwner,
            petWeightDeselectionViewModel.deselectionTrackerDisplay
        )()

        RecyclerViewPaginator(
            binding.weightsList,
            isLoading = { petWeightDeselectionViewModel.isLoading() },
            loadMore = { petWeightDeselectionViewModel.loadMore() },
            onLast = { petWeightDeselectionViewModel.onLastPage() }
        )

        binding.resetButton.setOnClickListener {
            petWeightDeselectionViewModel.reset()
        }

        binding.toKeepButton.setOnClickListener {
            petWeightDeselectionViewModel.toggleToKeepButton()
            setDisplay(
                toKeepButtonIsChecked = binding.toKeepButton.isChecked,
                toRemoveButtonIsChecked = binding.toRemoveButton.isChecked
            )
        }
        binding.toRemoveButton.setOnClickListener {
            petWeightDeselectionViewModel.toggleToRemoveButton()
            setDisplay(
                toKeepButtonIsChecked = binding.toKeepButton.isChecked,
                toRemoveButtonIsChecked = binding.toRemoveButton.isChecked
            )
        }

        petWeightDeselectionViewModel.toKeepButtonChecked.observe(viewLifecycleOwner) { isToggled ->
            val rightIconRes = if (isToggled) R.drawable.visibility_on_24px else R.drawable.visibility_off_24px
            val rightIcon = ContextCompat.getDrawable(requireContext(), rightIconRes)
            binding.toKeepButton.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.selection_to_keep),
                null,
                rightIcon,
                null
            )
        }

        petWeightDeselectionViewModel.toRemoveButtonChecked.observe(viewLifecycleOwner) { isToggled ->
            val rightIconRes = if (isToggled) R.drawable.visibility_on_24px else R.drawable.visibility_off_24px
            val rightIcon = ContextCompat.getDrawable(requireContext(), rightIconRes)
            binding.toRemoveButton.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.selection_to_remove),
                null,
                rightIcon,
                null
            )
        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                petWeightDeselectionViewModel.onQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                petWeightDeselectionViewModel.onQueryTextChanged(newText)
                return true
            }
        })

        return view
    }

    private fun setDisplay(toKeepButtonIsChecked: Boolean, toRemoveButtonIsChecked: Boolean) {
        if (toKeepButtonIsChecked && toRemoveButtonIsChecked) {
            Log.d("PetWeightDeselection", "Display ALL")
            petWeightDeselectionViewModel.deselectionTrackerDisplay.setDisplay(MultiDeselectionTracker.Display.All)
        }
        else if (!toKeepButtonIsChecked && toRemoveButtonIsChecked) {
            Log.d("PetWeightDeselection", "Display REMOVE")
            petWeightDeselectionViewModel.deselectionTrackerDisplay.setDisplay(MultiDeselectionTracker.Display.SelectionToRemove)
        }
        else if (toKeepButtonIsChecked && !toRemoveButtonIsChecked) {
            Log.d("PetWeightDeselection", "Display KEEP")
            petWeightDeselectionViewModel.deselectionTrackerDisplay.setDisplay(MultiDeselectionTracker.Display.SelectionToKeep)
        }
        else {
            Log.d("PetWeightDeselection", "Display NONE")
            petWeightDeselectionViewModel.deselectionTrackerDisplay.setDisplay(MultiDeselectionTracker.Display.None)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.weightsList.adapter = null
        _binding = null
    }
}