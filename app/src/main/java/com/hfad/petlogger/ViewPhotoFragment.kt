package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.databinding.FragmentViewPhotoBinding
import com.hfad.petlogger.databinding.FragmentViewPhotoDetailsBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsOfPhotoAlphabeticalOrderUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreEventsOfPhotoUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreNotesOfPhotoUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMorePetsOfPhotoUseCase
import com.hfad.petlogger.repositories.MediaRepository

class ViewPhotoFragment : Fragment() {
    private var _binding: FragmentViewPhotoBinding? = null
    val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPhotoBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val photoId = ViewPhotoFragmentArgs.fromBundle(requireArguments()).photoId
        val viewPhotoViewModel = ViewModelProvider(this, ViewPhotoViewModel.provideFactory(mediaRepository, photoId)).get(ViewPhotoViewModel::class.java)
        binding.viewPhotoViewModel = viewPhotoViewModel

        setAppBarTitle(getString(R.string.viewing_photo_details))

        val getPetsOfPhotoForDisplayUseCase = GetMorePetsOfPhotoUseCase(mediaRepository, photoId, petsAmt = 10)
        val associatedPetsDisplayViewModel = ViewModelProvider(this, AssociatedPetsDisplayViewModel.provideFactory(getPetsOfPhotoForDisplayUseCase)).get(AssociatedPetsDisplayViewModel::class.java)
        binding.associatedPetsDisplayViewModel = associatedPetsDisplayViewModel

        val getEventsOfPhotoForDisplayUseCase = GetMoreEventsOfPhotoUseCase(mediaRepository, photoId, eventAmt = 10)
        val associatedEventsDisplayViewModel = ViewModelProvider(this, AssociatedEventsDisplayViewModel.provideFactory(getEventsOfPhotoForDisplayUseCase)).get(AssociatedEventsDisplayViewModel::class.java)
        binding.associatedEventsDisplayViewModel = associatedEventsDisplayViewModel

        val getNotesOfPhoto = GetMoreNotesOfPhotoUseCase(mediaRepository, photoId, notesAmt = 10)
        val associatedNotesDisplayViewModel = ViewModelProvider(this, AssociatedNotesDisplayViewModel.provideFactory(getNotesOfPhoto)).get(AssociatedNotesDisplayViewModel::class.java)
        binding.associatedNotesDisplayViewModel = associatedNotesDisplayViewModel

        val getTagsOfPhotoAlphabeticalOrder = GetAllTagsOfPhotoAlphabeticalOrderUseCase(mediaRepository, photoId)
        val associatedTagsDisplayViewModel = ViewModelProvider(this, AssociatedTagsDisplayViewModel.provideFactory(getTagsOfPhotoAlphabeticalOrder)).get(AssociatedTagsDisplayViewModel::class.java)
        binding.associatedTagsDisplayViewModel = associatedTagsDisplayViewModel

        binding.viewPager.adapter = ViewPhotoViewPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle
        )
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.pets)
                2 -> getString(R.string.events)
                3 -> getString(R.string.notes)
                else -> null
            }
        }
        mediator?.attach()

        binding.editButton.setOnClickListener{
            findNavController().navigateSafe(ViewPhotoFragmentDirections.actionViewPhotoFragmentToEditPhotoFragment(photoId))
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }

    private class ViewPhotoViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> ViewPhotoDetailsFragment()
                1 -> AssociatedPetsDisplayFragment()
                2 -> AssociatedEventsDisplayFragment()
                3 -> AssociatedNotesDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class ViewPhotoDetailsFragment() : Fragment() {
    private var _binding: FragmentViewPhotoDetailsBinding? = null
    val binding: FragmentViewPhotoDetailsBinding get() = _binding!!
    private val viewPhotoViewModel: ViewPhotoViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPhotoDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewPhotoViewModel = viewPhotoViewModel

        viewPhotoViewModel.photo.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.photoDisplay)
            } else binding.photoDisplay.setImageResource(R.drawable.placeholder)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}