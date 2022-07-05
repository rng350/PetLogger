package com.hfad.guineapiglog

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hfad.guineapiglog.databinding.FragmentNewWeightBinding

class NewWeightFragment : Fragment() {
    private var _binding: FragmentNewWeightBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewWeightBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val weightDao = PetLoggerDatabase.getInstance(application).weightDao
        val petDao = PetLoggerDatabase.getInstance(application).petDao

        val petIdArg = NewWeightFragmentArgs.fromBundle(requireArguments()).petId

        val viewModelFactory = NewWeightViewModelFactory(weightDao, petDao, petIdArg?.toLong())
        val viewModel = ViewModelProvider(this, viewModelFactory).get(NewWeightViewModel::class.java)
        val petSelectorDialog = PetSingleSelectorDialogFragment(viewModel = viewModel)
        val datePicker = DatePicker.generate(viewModel)
        val timePicker = TimePicker.generate(viewModel, requireContext())

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.inputAddPetButton.setOnClickListener {
            petSelectorDialog.show(childFragmentManager, "PET_SELECTOR")
        }

        binding.inputWeightDateButton.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.inputWeightTimeButton.setOnClickListener {
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        binding.submitWeightButton.setOnClickListener {
            viewModel.submitWeight()
        }

        binding.backButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_newWeightFragment_to_homeFragment)
        }

        return view
    }
}