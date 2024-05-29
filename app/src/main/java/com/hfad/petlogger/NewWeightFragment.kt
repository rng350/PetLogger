package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentNewWeightBinding

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
        //val timePicker = TimePicker.generate(viewModel.wDateTime, requireContext())

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setAppBarTitle(getString(R.string.new_weight_header))

        binding.inputAddPetButton.setOnClickListener {
            PetSingleSelectorDialogFragment.newInstance(viewModel).show(childFragmentManager, "PET_SELECTOR")

            /*if (petSelectorDialog.isVisible) {
                Log.e("dialog_visible", "gonna dismiss")
                petSelectorDialog.dismiss()
            } else {
                Log.e("dialog_not_visible", "gonna remake")
                petSelectorDialog.show(childFragmentManager, "PET_SELECTOR")
            }*/
        }

        binding.inputWeightDateButton.setOnClickListener {
            DatePicker.generate(viewModel.wDateTime).show(parentFragmentManager, "DATE_PICKER")
        }

        binding.inputWeightTimeButton.setOnClickListener {
            TimePicker.generate(viewModel.wDateTime, requireContext()).show(parentFragmentManager, "TIME_PICKER")
        }

        binding.submitWeightButton.setOnClickListener {
            viewModel.submitWeight()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }
}