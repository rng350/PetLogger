package com.hfad.guineapiglog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hfad.guineapiglog.databinding.FragmentViewEventBinding
import com.hfad.guineapiglog.databinding.PetItemBinding

class ViewEventFragment : Fragment() {
    private var _binding: FragmentViewEventBinding? = null
    private val binding get() = _binding!!

    private var _galleryDisplay: GalleryDisplay? = null
    private val galleryDisplay get() = _galleryDisplay!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewEventBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val eventDao = PetLoggerDatabase.getInstance(application).eventDao

        val eventId = ViewEventFragmentArgs.fromBundle(requireArguments()).eventId

        val viewModelFactory = ViewEventViewModelFactory(eventDao, eventId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ViewEventViewModel::class.java)

        Log.e("newEventFrag", "before gallery display viewmodel")
        val galleryDisplayViewModelFactory = GalleryDisplayViewModelFactory(eventId, PhotosOfEventFetcher(eventDao))
        val galleryDisplayViewModel = ViewModelProvider(this, galleryDisplayViewModelFactory).get(GalleryDisplayViewModel::class.java)
        Log.e("newEventFrag", "after gallery display viewmodel")

        _galleryDisplay = GalleryDisplay(this, binding.galleryDisplay, galleryDisplayViewModel)
        galleryDisplay.onCreate(savedInstanceState)

        binding.viewModel = viewModel
        binding.galleryDisplayViewModel = galleryDisplayViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        /*val petAdapter = BindingInterfaceCreator.setupNavigatablePetAdapter(viewModel.petNavigator)
        binding.petsList.adapter = petAdapter
        viewModel.petsAssociated.observe(viewLifecycleOwner, Observer {
            it?.let {
                petAdapter.submitList(it)
            }
        })*/
        BindingInterfaceCreator.setupPetWithProfilePhotoAdapter(
            viewModel.petsAssociated,
            binding.petsList,
            viewLifecycleOwner,
            requireContext(),
            viewModel.petNavigator)

        viewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = ViewEventFragmentDirections.actionViewEventFragmentToViewPetFragment(it)
                this.findNavController().navigate(action)
                viewModel.petNavigator.onNavigated()
            }
        })

        binding.editEventButton.setOnClickListener {
            this.findNavController().navigate(ViewEventFragmentDirections.actionViewEventFragmentToEditEventFragment(eventId))
        }

        binding.backButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_viewEventFragment_to_homeFragment)
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}