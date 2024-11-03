package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.databinding.FragmentViewWeightBinding
import com.hfad.petlogger.databinding.FragmentViewWeightDetailsBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsOfWeightAlphabeticalOrderUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreNotesOfWeightUseCase
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.util.GetDateTimeDisplayUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineLatest
import kotlinx.coroutines.launch


class ViewWeightFragment : Fragment() {
    private var _binding: FragmentViewWeightBinding? = null
    val binding: FragmentViewWeightBinding get() = _binding!!
    lateinit var viewModel: ViewWeightViewModel
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewWeightBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val weightId = ViewWeightFragmentArgs.fromBundle(requireArguments()).weightId
        val weightRepository = WeightRepository(database)
        viewModel = ViewModelProvider(this, ViewWeightViewModel.provideFactory(weightRepository, weightId)).get(ViewWeightViewModel::class.java)
        binding.viewModel = viewModel

        val getNotesOfWeight = GetMoreNotesOfWeightUseCase(weightRepository, weightId, notesAmt = 10)
        val associatedNotesDisplayViewModel = ViewModelProvider(this, AssociatedNotesDisplayViewModel.provideFactory(getNotesOfWeight)).get(AssociatedNotesDisplayViewModel::class.java)
        binding.associatedNotesDisplayViewModel = associatedNotesDisplayViewModel

        val getTagsOfWeight = GetAllTagsOfWeightAlphabeticalOrderUseCase(weightRepository, weightId)
        val associatedTagsDisplayViewModel = ViewModelProvider(this, AssociatedTagsDisplayViewModel.provideFactory(getTagsOfWeight)).get(AssociatedTagsDisplayViewModel::class.java)
        binding.associatedTagsDisplayViewModel = associatedTagsDisplayViewModel

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fullWeightDetails.collectLatest { weightState ->
                    weightState?.let {
                        if (activity != null && isAdded) {
                            // App Bar Title
                            val topAppBarTitleRemainder = if (it.weightPet.petName[it.weightPet.petName.length-1].lowercaseChar() != 's') "\'s Weight" else "\' Weight"
                            val title = "${it.weightPet.petName}${topAppBarTitleRemainder}"
                            val subtitle = it.curWeight.weightDateTimeDisplay
                            setAppBarTitle(title = title, subtitle = subtitle)
                        }
                    }
                }
            }
        }

        binding.editWeight.setOnClickListener{
            viewModel.fullWeightDetails.value?.let { weight ->
                findNavController().navigateSafe(ViewWeightFragmentDirections.actionViewWeightFragmentToEditWeightFragment(weightId = weightId, petId = weight.weightPet.petId))
            }
        }

        binding.back.setOnClickListener{
            findNavController().popBackStack()
        }

        associatedNotesDisplayViewModel.noteNavigator.navigateTo.observe(viewLifecycleOwner, Observer { noteId ->
            noteId?.let {
                associatedNotesDisplayViewModel.noteNavigator.onNavigated()
                findNavController().navigateSafe(ViewWeightFragmentDirections.actionViewWeightFragmentToViewNoteFragment(noteId))
            }
        })
        associatedTagsDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner, Observer { tagId ->
            tagId?.let {
                associatedTagsDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewWeightFragmentDirections.actionViewWeightFragmentToViewTagFragment(tagId))
            }
        })

        binding.viewPager.adapter = ViewWeightViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                else -> null
            }
        }
        mediator?.attach()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }

    private class ViewWeightViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> ViewWeightDetailsFragment()
                1 -> AssociatedNotesDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }

    }
}

class ViewWeightDetailsFragment(): Fragment() {
    private var _binding : FragmentViewWeightDetailsBinding? = null
    val binding: FragmentViewWeightDetailsBinding get() = _binding!!
    private val viewModel: ViewWeightViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewWeightDetailsBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fullWeightDetails.collectLatest { weightState ->
                    weightState?.let {
                        if (activity != null && isAdded) {
                            // Profile Pic
                            it.weightPet.petProfilePicUri?.let {photoUri ->
                                Glide.with(context)
                                    .load(photoUri)
                                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                                    .into(binding.petProfileImage)
                            }
                            // Weight Notes?
                            if (it.curWeight.notes.isNotEmpty()) {
                                binding.weightNotesCard.visibility = View.VISIBLE
                            }
                            // Previous Weight card
                            if (it.prevWeight != null) {
                                binding.noPreviousWeightCard.visibility = View.GONE
                                binding.previousWeightCard.visibility = View.VISIBLE
                            } else {
                                binding.noPreviousWeightCard.visibility = View.VISIBLE
                                binding.previousWeightCard.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
        binding.petCard.setOnClickListener {
            viewModel.fullWeightDetails.value?.let {
                findNavController().navigateSafe(ViewWeightFragmentDirections.actionViewWeightFragmentToViewPetFragment(it.weightPet.petId))
            }
        }
        binding.previousWeightCard.setOnClickListener {
            viewModel.fullWeightDetails.value?.prevWeight?.let {
                findNavController().navigateSafe(ViewWeightFragmentDirections.actionViewWeightFragmentSelf(it.id))
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val items = listOf("g", "kg", "lb", "oz")

        val arrayAdapter = ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, items)
        binding.weightUnitDropDownList.setAdapter(arrayAdapter)
        arrayAdapter.notifyDataSetChanged()

        binding.weightUnitDropDownList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            viewModel.setWeightUnit(parent.getItemAtPosition(position).toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}