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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.databinding.FragmentViewTagBinding
import com.hfad.petlogger.databinding.FragmentViewTaggedEventsBinding
import com.hfad.petlogger.databinding.FragmentViewTaggedNotesBinding
import com.hfad.petlogger.databinding.FragmentViewTaggedPetsBinding
import com.hfad.petlogger.databinding.FragmentViewTaggedPhotosBinding
import com.hfad.petlogger.databinding.FragmentViewTaggedWeightsBinding
import com.hfad.petlogger.recyclerviews.SetupAssociatedEventsDisplayUseCase
import com.hfad.petlogger.recyclerviews.SetupAssociatedNotesDisplayUseCase
import com.hfad.petlogger.recyclerviews.SetupAssociatedPetsDisplayUseCase
import com.hfad.petlogger.recyclerviews.SetupAssociatedPhotosDisplayUseCase
import com.hfad.petlogger.recyclerviews.SetupAssociatedWeightsDisplayUseCase
import com.hfad.petlogger.repositories.TagRepository

class ViewTagFragment : Fragment() {
    private var _binding: FragmentViewTagBinding? = null
    val binding: FragmentViewTagBinding get() = _binding!!
    private var mediator: TabLayoutMediator? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewTagBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val tagRepository = TagRepository(database)
        val tagId = ViewTagFragmentArgs.fromBundle(requireArguments()).tagId
        val viewTagViewModel = ViewModelProvider(this, ViewTagViewModel.provideFactory(tagRepository, tagId)).get(ViewTagViewModel::class.java)
        binding.viewTagViewModel = viewTagViewModel

        viewTagViewModel.tag.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(
                    title=it.tagName,
                    subtitle = getString(R.string.viewing_tagged_content)
                )
            }
        })

        binding.viewPager.adapter = ViewTagViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.pets)
                1 -> getString(R.string.weights)
                2 -> getString(R.string.events)
                3 -> getString(R.string.notes)
                4 -> getString(R.string.media)
                else -> null
            }
        }
        mediator?.attach()

        viewTagViewModel.petNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewTagViewModel.petNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewPetFragment(it))
            }
        })
        viewTagViewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewTagViewModel.weightNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewWeightFragment(it))
            }
        })
        viewTagViewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewTagViewModel.eventNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewEventFragment(it))
            }
        })
        viewTagViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewTagViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewNoteFragment(it))
            }
        })
        viewTagViewModel.photoNavigator.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewTagViewModel.photoNavigator.onNavigated()
                findNavController().navigateSafe(ViewTagFragmentDirections.actionViewTagFragmentToViewPhotoFragment(it))
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }
}

class TaggedPetsListFragment : Fragment() {
    private var _binding: FragmentViewTaggedPetsBinding? = null
    val binding: FragmentViewTaggedPetsBinding get() = _binding!!
    private val viewTagViewModel: ViewTagViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewTaggedPetsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.viewTagViewModel = viewTagViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        SetupAssociatedPetsDisplayUseCase(
            pets = viewTagViewModel.taggedPets,
            petNavigator = viewTagViewModel.petNavigator,
            recyclerView = binding.petsList,
            context = application.applicationContext,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.petsList?.adapter = null
        _binding = null
    }
}

class TaggedEventsListFragment : Fragment() {
    private var _binding: FragmentViewTaggedEventsBinding? = null
    val binding: FragmentViewTaggedEventsBinding get() = _binding!!
    val viewTagViewModel: ViewTagViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewTaggedEventsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.viewTagViewModel = viewTagViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupAssociatedEventsDisplayUseCase(
            events = viewTagViewModel.taggedEvents,
            eventNavigator = viewTagViewModel.eventNavigator,
            recyclerView = binding.eventsList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.eventsList?.adapter = null
        _binding = null
    }
}

class TaggedWeightsListFragment : Fragment() {
    private var _binding: FragmentViewTaggedWeightsBinding? = null
    val binding: FragmentViewTaggedWeightsBinding get() = _binding!!
    private val viewTagViewModel: ViewTagViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewTaggedWeightsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.viewTagViewModel = viewTagViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application

        SetupAssociatedWeightsDisplayUseCase(
            weights = viewTagViewModel.taggedWeights,
            weightNavigator = viewTagViewModel.weightNavigator,
            recyclerView = binding.weightsList,
            context = application.applicationContext,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.weightsList?.adapter = null
        _binding = null
    }
}

class TaggedNotesListFragment : Fragment() {
    private var _binding: FragmentViewTaggedNotesBinding? = null
    val binding: FragmentViewTaggedNotesBinding get() = _binding!!
    private val viewTagViewModel: ViewTagViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewTaggedNotesBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.viewTagViewModel = viewTagViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        SetupAssociatedNotesDisplayUseCase(
            notes = viewTagViewModel.taggedNotes,
            noteNavigator = viewTagViewModel.noteNavigator,
            recyclerView = binding.notesList,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.notesList?.adapter = null
        _binding = null
    }
}

class TaggedMediaListFragment : Fragment() {
    private var _binding: FragmentViewTaggedPhotosBinding? = null
    val binding: FragmentViewTaggedPhotosBinding get() = _binding!!
    private val viewTagViewModel: ViewTagViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewTaggedPhotosBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.viewTagViewModel = viewTagViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application

        SetupAssociatedPhotosDisplayUseCase(
            photos = viewTagViewModel.taggedPhotos,
            photoNavigator = viewTagViewModel.photoNavigator,
            recyclerView = binding.photoList,
            context = application.applicationContext,
            lifecycleScope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.photoList?.adapter = null
        _binding = null
    }
}

class ViewTagViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TaggedPetsListFragment()
            1 -> TaggedWeightsListFragment()
            2 -> TaggedEventsListFragment()
            3 -> TaggedNotesListFragment()
            4 -> TaggedMediaListFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}