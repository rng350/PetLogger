package com.hfad.petlogger.screens.sections.associatedentities

import RecyclerViewPaginator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hfad.petlogger.databinding.FragmentPetListBinding
import com.hfad.petlogger.screens.pet.PetListViewModel
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedPetsDisplayUseCase

class AssociatedPetsDisplayFragment : Fragment() {
    private var _binding: FragmentPetListBinding? = null
    val binding: FragmentPetListBinding get() = _binding!!

    private val petListViewModel: PetListViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = petListViewModel

        binding.petListTopAppBarLayout.visibility = View.GONE

        SetupAssociatedPetsDisplayUseCase(
            petListViewModel.pets,
            petListViewModel.petNavigator,
            binding.petsList,
            requireContext(),
            lifecycleScope,
            viewLifecycleOwner
        )()

        RecyclerViewPaginator(
            recyclerView = binding.petsList,
            isLoading = { petListViewModel.isLoading() },
            loadMore = { petListViewModel.load() },
            onLast = { petListViewModel.onLastPage() }
        )

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                petListViewModel.onQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                petListViewModel.onQueryTextChanged(newText)
                return true
            }
        })

        // Creating a brand new pet from an event/note/photo just doesn't feel right
        binding.addPetButton.visibility = View.GONE

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}