package com.hfad.petlogger.screens.pet.petsingleselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentPetSingleSelectionDisplayBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PetSingleSelectionDisplayFragment : Fragment() {
    private var _binding: FragmentPetSingleSelectionDisplayBinding? = null
    val binding: FragmentPetSingleSelectionDisplayBinding get() = _binding!!
    val petSingleSelectionViewModel: PetSingleSelectionViewModel by viewModels ({ requireParentFragment().requireParentFragment() })
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetSingleSelectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.petSingleSelectionViewModel = petSingleSelectionViewModel

        binding.addPetButton.setOnClickListener { button ->
            button.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                PetSingleSelectionDialogFragment().show(childFragmentManager, "PET_SINGLE_PICKER")
                delay(200)
                button.isEnabled = true
            }
        }

        binding.resetButton.setOnClickListener {
            petSingleSelectionViewModel.resetSelection()
        }

        if (petSingleSelectionViewModel.initialPetSelectedId == null) {
            binding.resetButton.isEnabled = false
            binding.resetButton.visibility = View.GONE
        }

        petSingleSelectionViewModel.selectionTracker.currentSelection.observe(viewLifecycleOwner) {checkablePetWithPhoto ->
            if (checkablePetWithPhoto != null) {
                val photoUri = checkablePetWithPhoto.item.petProfilePicUri
                if (photoUri != null) {
                    Glide.with(requireContext())
                        .load(photoUri)
                        .into(binding.petProfileImage)
                }
                else {
                    binding.petProfileImage.setImageResource(R.drawable.placeholder)
                }
                binding.petName.text = checkablePetWithPhoto.item.petName

                binding.petCard.visibility = View.VISIBLE
                binding.noPetSelected.visibility = View.GONE
            } else {
                binding.petCard.visibility = View.GONE
                binding.noPetSelected.visibility = View.VISIBLE
            }
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        binding.addPetButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}