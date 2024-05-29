package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentFullGalleryBinding
import com.hfad.petlogger.databinding.FragmentPetListBinding
import com.hfad.petlogger.recyclerviews.BindingInterfaceCreator

class FullGalleryFragment : Fragment() {
    private var _binding: FragmentFullGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FullGalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFullGalleryBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val photoDao = PetLoggerDatabase.getInstance(application).photoDao

        val viewModelFactory = FullGalleryViewModelFactory(photoDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(FullGalleryViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setAppBarTitle(getString(R.string.media_gallery_header))

        BindingInterfaceCreator.setupGalleryPhotoItemAdapter(
            viewModel.photos,
            binding.gallery,
            viewLifecycleOwner,
            requireContext(),
            viewModel.photoNavigator)

        binding.addPhotoButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_petListFragment_to_newPetFragment)
        }
        viewModel.photoNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                // TODO: Implement
                /*val action = PetListFragmentDirections.ACTION_TRANSFER_TO_VIEW_PHOTO_OR_WHATEVER(it)
                this.findNavController().navigate(action)*/
                viewModel.photoNavigator.onNavigated()
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}