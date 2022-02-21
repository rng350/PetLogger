package com.hfad.guineapiglog

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.hfad.guineapiglog.databinding.FragmentNewPetBinding

class NewPetFragment : Fragment() {
    private var _binding: FragmentNewPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val petDao = PetLoggerDatabase.getInstance(application).petDao
        val viewModelFactory = NewPetViewModelFactory(petDao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(NewPetViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.submit.setOnClickListener {
            // petDao.insert()
        }

        binding.back.setOnClickListener {
            this.findNavController().navigate(R.id.action_newPetFragment_to_homeFragment)
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select pet's date of birth")
                        .build()

        binding.inputDOBButton.setOnClickListener {
            datePicker.show(getParentFragmentManager(), "DATE_PICKER")
        }

        return view
    }

}