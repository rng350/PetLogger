package com.hfad.petlogger.screens.pet

import RecyclerViewPaginator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.databinding.FragmentPetListBinding
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfAllPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfSearchedPetsUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedPetsDisplayUseCase

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
        val database = PetLoggerDatabase.getInstance(application)

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getAllPets = GetMoreOfAllPetsUseCase(petRepository, petsAmt = 18)
        val getSearchedPetsFromAll = GetMoreOfSearchedPetsUseCase(database.petDao, petsAmt=10)
        viewModel = ViewModelProvider(this, PetListViewModel.provideFactory(getAllPets, getSearchedPetsFromAll)).get(
            PetListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupAssociatedPetsDisplayUseCase(
            viewModel.pets,
            viewModel.petNavigator,
            binding.petsList,
            application.applicationContext,
            lifecycleScope,
            viewLifecycleOwner
        )()

        RecyclerViewPaginator(
            recyclerView = binding.petsList,
            loadMore = {viewModel.load()},
            isLoading = {viewModel.isLoading()},
            onLast = {viewModel.onLastPage()}
        )

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onQueryTextChanged(newText)
                return true
            }
        })

        if (findNavController().previousBackStackEntry == null) {
            binding.petListTopAppBar.navigationIcon = null
        } else {
            binding.petListTopAppBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.addPetButton.setOnClickListener {
            findNavController().navigateSafe(PetListFragmentDirections.actionPetListFragmentToNewPetFragment())
        }
        viewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = PetListFragmentDirections.actionPetListFragmentToViewPetFragment(it)
                this.findNavController().navigateSafe(action)
                viewModel.petNavigator.onNavigated()
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}