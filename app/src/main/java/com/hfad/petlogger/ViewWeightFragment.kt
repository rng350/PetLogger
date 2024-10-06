package com.hfad.petlogger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.databinding.FragmentViewWeightBinding
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
    val binding get() = _binding!!
    lateinit var viewModel: ViewWeightViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewWeightBinding.inflate(inflater, container, false)
        val view = binding.root
        val application = requireActivity().application
        val database = PetLoggerDatabase.getInstance(application)
        val weightId = ViewWeightFragmentArgs.fromBundle(requireArguments()).weightId

        val weightRepository = WeightRepository(database)
        viewModel = ViewModelProvider(this, ViewWeightViewModel.provideFactory(weightRepository, weightId)).get(ViewWeightViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

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

        val getNotesOfWeight = GetMoreNotesOfWeightUseCase(weightRepository, weightId, notesAmt = 10)
        val associatedNotesDisplayViewModel = ViewModelProvider(this, AssociatedNotesDisplayViewModel.provideFactory(getNotesOfWeight)).get(AssociatedNotesDisplayViewModel::class.java)
        binding.associatedNotesDisplayViewModel = associatedNotesDisplayViewModel

        binding.editWeight.setOnClickListener{
            viewModel.fullWeightDetails.value?.let { weight ->
                findNavController().navigateSafe(ViewWeightFragmentDirections.actionViewWeightFragmentToEditWeightFragment(weightId = weightId, petId = weight.weightPet.petId))
            }
        }

        binding.back.setOnClickListener{
            findNavController().popBackStack()
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