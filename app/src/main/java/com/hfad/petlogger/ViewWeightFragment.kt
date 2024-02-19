package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.databinding.FragmentViewWeightBinding


class ViewWeightFragment : Fragment() {
    private var _binding: FragmentViewWeightBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewWeightBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireActivity().application

        val weightDao = PetLoggerDatabase.getInstance(application).weightDao
        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val weightId = ViewWeightFragmentArgs.fromBundle(requireArguments()).weightId

        val viewModelFactory = ViewWeightViewModelFactory(weightDao, petDao, weightId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ViewWeightViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        
        viewModel.prevWeight.observe(viewLifecycleOwner, Observer { prevWeight ->
            viewModel.weight.value?.let { newerWeight ->
                viewModel.prevWeight.value?.let { prevWeight ->
                    binding.previousWeightCard.visibility = View.VISIBLE

                    if (newerWeight.weightGrams > prevWeight.weightGrams) {
                        binding.differenceFromPrevWeight.text = getString(R.string.weight_difference_plus,
                            (newerWeight.weightGrams - prevWeight.weightGrams).toString())
                    } else if (newerWeight.weightGrams < prevWeight.weightGrams) {
                        binding.differenceFromPrevWeight.text = getString(R.string.weight_difference_minus,
                            (prevWeight.weightGrams - newerWeight.weightGrams).toString())
                    } else {
                        binding.differenceFromPrevWeight.text = getString(R.string.weight_difference_no_change)
                    }
                }
            } 
        })

        viewModel.weight.observe(viewLifecycleOwner, Observer {
            viewModel.weight.value?.let {
                if (it.weightNotes.isNotEmpty()) {
                    binding.weightNotesCard.visibility = View.VISIBLE
                }
            }
        })

        return view
    }
}