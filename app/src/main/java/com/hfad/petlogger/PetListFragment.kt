package com.hfad.petlogger

import RecyclerViewPaginator
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentPetListBinding
import com.hfad.petlogger.photodisplay.stateful.GetAllPetsForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreOfAllPetsUseCase
import com.hfad.petlogger.recyclerviews.SetupAssociatedPetsDisplayUseCase
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository

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
        val getAllPets = GetMoreOfAllPetsUseCase(petRepository, petsAmt = 10)
        viewModel = ViewModelProvider(this, PetListViewModel.provideFactory(getAllPets)).get(PetListViewModel::class.java)
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

        setAppBarTitle(getString(R.string.pet_list_header))

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.petsList.adapter = null
        _binding = null
    }
}