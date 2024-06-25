package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEventListBinding
import com.hfad.petlogger.databinding.FragmentPetListBinding
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator

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
        val application = requireNotNull(this.activity).application
        val eventDao = PetLoggerDatabase.getInstance(application).eventDao

        val viewModelFactory = EventListViewModelFactory(eventDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EventListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val eventAdapter = BindingInterfaceCreator.setupNavigatableEventAdapter(viewModel.eventNavigator)
        binding.eventsList.adapter = eventAdapter
        binding.addEventButton.setOnClickListener {
            this.findNavController().navigate(EventListFragmentDirections.actionEventListFragmentToNewEventFragment())
        }

        setAppBarTitle(getString(R.string.event_list_header))

        // TODO: Put that shit away
        viewModel.events.observe(viewLifecycleOwner, Observer {
            it?.let {
                println("events...")
                eventAdapter.submitList(it)
                println("events: ${it.toString()}")
            }
        })
        viewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {eventID ->
            eventID?.let {
                this.findNavController().navigate(EventListFragmentDirections.actionEventListFragmentToViewEventFragment(it))
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