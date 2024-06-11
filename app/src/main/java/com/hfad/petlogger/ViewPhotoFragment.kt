package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentViewPhotoBinding
import com.hfad.petlogger.repositories.MediaRepository

class ViewPhotoFragment : Fragment() {
    private var _binding: FragmentViewPhotoBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPhotoBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, requireContext())
        val photoId = ViewPhotoFragmentArgs.fromBundle(requireArguments()).photoId
        val viewPhotoViewModel = ViewModelProvider(this, ViewPhotoViewModel.provideFactory(mediaRepository, photoId)).get(ViewPhotoViewModel::class.java)
        binding.viewPhotoViewModel = viewPhotoViewModel

        setAppBarTitle(getString(R.string.viewing_photo_details))

        viewPhotoViewModel.photo.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.photoDisplay)
            } else binding.photoDisplay.setImageResource(R.drawable.placeholder)
        }

        binding.editButton.setOnClickListener{
            findNavController().navigate(ViewPhotoFragmentDirections.actionViewPhotoFragmentToEditPhotoFragment(photoId))
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}