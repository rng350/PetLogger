package com.hfad.petlogger.screens.event

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
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.R
import com.hfad.petlogger.databinding.FragmentEventListBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.events.usecases.GetMoreOfAllEventsUseCase
import com.hfad.petlogger.events.EventRepository
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.common.setAppBarTitle
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
        setAppBarTitle(getString(R.string.event_list_header))

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val eventRepository = EventRepository(database, mediaRepository)
        val getAllEvents = GetMoreOfAllEventsUseCase(eventRepository, eventAmt=10)
        viewModel = ViewModelProvider(this, EventListViewModel.provideFactory(getAllEvents)).get(
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