package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNewWeightBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllCheckablePetsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllNotesUseCase
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewWeightFragment : Fragment() {
    private var _binding: FragmentNewWeightBinding? = null
    private val binding get() = _binding!!
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
        newWeightViewModel = ViewModelProvider(this, NewWeightViewModel.provideFactory(weightRepository)).get(NewWeightViewModel::class.java)
        binding.newWeightViewModel = newWeightViewModel

        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val petId = NewWeightFragmentArgs.fromBundle(requireArguments()).petId?.toLongOrNull()
        val petIdAsList = if (petId != null) listOf(petId) else listOf()
        val getCheckablePets = GetAllCheckablePetsUseCase(petRepository, petIdAsList)
        val petSingleSelectionViewModel =  ViewModelProvider(this, PetSingleSelectionViewModel.provideFactory(getCheckablePets, petId)).get(PetSingleSelectionViewModel::class.java)
        binding.petSingleSelectionViewModel = petSingleSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotesUseCase = GetAllNotesUseCase(noteRepository)
        val noteMultiSelectionViewModel = ViewModelProvider(this, NoteMultiSelectionViewModel.provideFactory(getAllNotesUseCase)).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        setAppBarTitle(getString(R.string.new_weight_header))

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
                TimePicker.generate(newWeightViewModel.weightDateTime, requireContext()).show(parentFragmentManager, "TIME_PICKER")
                delay(200)
                button.isEnabled = true
            }
        }

        binding.submitWeightButton.setOnClickListener {
            petSingleSelectionViewModel.selectionTracker.currentSelection.value?.item?.petId?.let{ petId ->
                newWeightViewModel.submitWeight(petId = petId, notes = noteMultiSelectionViewModel.getNotesToAdd())
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
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