package com.hfad.petlogger.screens.weight.newweight

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.DatePicker
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.screens.pet.petsingleselection.PetSingleSelectionViewModel
import com.hfad.petlogger.R
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.common.TimePicker
import com.hfad.petlogger.databinding.FragmentNewWeightBinding
import com.hfad.petlogger.databinding.FragmentNewWeightDetailsBinding
import com.hfad.petlogger.pets.usecases.GetAllCheckablePetsUseCase
import com.hfad.petlogger.notes.usecases.GetAllNotesUseCase
import com.hfad.petlogger.tags.usecases.GetAllTagsUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.weights.WeightRepository
import com.hfad.petlogger.common.setAppBarTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewWeightFragment : Fragment() {
    private var _binding: FragmentNewWeightBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null
    lateinit var newWeightViewModel: NewWeightViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewWeightBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)

        val weightRepository = WeightRepository(database)
        newWeightViewModel = ViewModelProvider(this,
            NewWeightViewModel.provideFactory(weightRepository)
        ).get(NewWeightViewModel::class.java)
        binding.newWeightViewModel = newWeightViewModel

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val petId = NewWeightFragmentArgs.fromBundle(requireArguments()).petId?.toLongOrNull()
        val petIdAsList = if (petId != null) listOf(petId) else listOf()
        val getCheckablePets = GetAllCheckablePetsUseCase(petRepository, petIdAsList)
        val petSingleSelectionViewModel =  ViewModelProvider(this,
            PetSingleSelectionViewModel.provideFactory(getCheckablePets, petId)
        ).get(PetSingleSelectionViewModel::class.java)
        binding.petSingleSelectionViewModel = petSingleSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotesUseCase = GetAllNotesUseCase(noteRepository)
        val noteMultiSelectionViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(getAllNotesUseCase)
        ).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags)
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        setAppBarTitle(getString(R.string.new_weight_header))

        binding.viewPager.adapter = NewWeightViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                else -> null
            }
        }
        mediator?.attach()

        binding.submitWeightButton.setOnClickListener {
            petSingleSelectionViewModel.selectionTracker.currentSelection.value?.item?.petId?.let{ petId ->
                newWeightViewModel.submitWeight(
                    petId = petId,
                    notes = noteMultiSelectionViewModel.getNotesToAdd(),
                    tags = tagMultiSelectionViewModel.getTagsToAdd()
                )
            }
        }

        binding.backButton.setOnClickListener {
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

    private class NewWeightViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> NewWeightDetailsFragment()
                1 -> NoteMultiSelectionDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class NewWeightDetailsFragment() : Fragment() {
    private var _binding: FragmentNewWeightDetailsBinding? = null
    val binding: FragmentNewWeightDetailsBinding get() = _binding!!
    private val newWeightViewModel: NewWeightViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewWeightDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.newWeightViewModel = newWeightViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.dateFieldText.setOnClickListener { button ->
            button.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                DatePicker.generate(newWeightViewModel.weightDateTime).show(parentFragmentManager, "DATE_PICKER")
                delay(200)
                button.isEnabled = true
            }
        }

        binding.timeFieldText.setOnClickListener { button ->
            button.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                TimePicker.generate(newWeightViewModel.weightDateTime, requireContext())
                    .show(parentFragmentManager, "TIME_PICKER")
                delay(200)
                button.isEnabled = true
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val items = listOf("grams", "kilograms", "pounds", "ounces")

        val arrayAdapter = ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, items)
        binding.weightUnitDropDownList.setAdapter(arrayAdapter)
        arrayAdapter.notifyDataSetChanged()

        binding.weightUnitDropDownList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            newWeightViewModel.setWeightUnitType(parent.getItemAtPosition(position).toString())
        }
    }

    override fun onStop() {
        super.onStop()
        binding.dateFieldText.isEnabled = true
        binding.timeFieldText.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}