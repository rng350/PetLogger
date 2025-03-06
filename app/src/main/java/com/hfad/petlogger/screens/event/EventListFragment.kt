package com.hfad.petlogger.screens.event

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
import com.hfad.petlogger.databinding.FragmentEventListBinding
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.events.domain.usecases.GetMoreOfAllEventsUseCase
import com.hfad.petlogger.events.domain.usecases.GetMoreOfSearchedEventsUseCase
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.screens.sections.recyclerviews.SetupAssociatedEventsDisplayUseCase

class EventListFragment : Fragment() {
    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EventListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val eventRepository = EventRepository(database, mediaRepository)
        val getAllEvents = GetMoreOfAllEventsUseCase(eventRepository, eventAmt=10)
        val getSearchedEvents = GetMoreOfSearchedEventsUseCase(database.eventDao, eventAmt=10)
        viewModel = ViewModelProvider(this, EventListViewModel.provideFactory(getAllEvents, getSearchedEvents)).get(
            EventListViewModel::class.java)
        binding.viewModel = viewModel

        SetupAssociatedEventsDisplayUseCase(
            viewModel.event,
            viewModel.eventNavigator,
            binding.eventsList,
            lifecycleScope,
            viewLifecycleOwner
        )()

        RecyclerViewPaginator(
            recyclerView = binding.eventsList,
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
            binding.eventListTopAppBar.navigationIcon = null
        } else {
            binding.eventListTopAppBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.addEventButton.setOnClickListener {
            this.findNavController().navigateSafe(EventListFragmentDirections.actionEventListFragmentToNewEventFragment())
        }
        viewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {eventID ->
            eventID?.let {
                this.findNavController().navigateSafe(EventListFragmentDirections.actionEventListFragmentToViewEventFragment(it))
                viewModel.eventNavigator.onNavigated()
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.eventsList.adapter = null
        _binding = null
    }
}