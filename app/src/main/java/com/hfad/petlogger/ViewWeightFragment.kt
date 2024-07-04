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
import com.hfad.petlogger.repositories.WeightRepository
import kotlinx.coroutines.flow.collectLatest
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
                viewModel.assocPet.collectLatest {
                    it?.let {
                        // set title
                        val topAppBarTitleRemainder = if (it.pet.petName[it.pet.petName.length-1].lowercaseChar() != 's') "\'s Weight" else "\' Weight"
                        setAppBarTitle(title = "${it.pet.petName}${topAppBarTitleRemainder}", subtitle = "${viewModel.weight.value!!.weightDateTime}")
                        // set profile pic
                        it.profilePic?.let {photo ->
                            Glide.with(context)
                                .load(photo.contentUri)
                                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                                .into(binding.petProfileImage)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weight.collectLatest {
                    it?.let {
                        if (it.weightNotes.isNotEmpty()) {
                            binding.weightNotesCard.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prevWeight.collectLatest {
                    if (it != null) {
                        Log.d("prevWeight", "GOT SOMETHING: ${it}")
                        binding.noPreviousWeightCard.visibility = View.GONE
                        binding.previousWeightCard.visibility = View.VISIBLE
                    } else {
                        Log.d("prevWeight", "GOT NUTHIN!")
                        binding.noPreviousWeightCard.visibility = View.VISIBLE
                        binding.previousWeightCard.visibility = View.GONE
                    }
                }
            }
        }

        binding.petCard.setOnClickListener {
            viewModel.assocPet.value?.let {
                findNavController().navigate(ViewWeightFragmentDirections.actionViewWeightFragmentToViewPetFragment(it.pet.petID))
            }
        }

        binding.previousWeightCard.setOnClickListener {
            viewModel.prevWeight.value?.let {
                findNavController().navigate(ViewWeightFragmentDirections.actionViewWeightFragmentSelf(it.id))
            }
        }

        binding.editWeight.setOnClickListener{
            findNavController().navigate(ViewWeightFragmentDirections.actionViewWeightFragmentToEditWeightFragment(weightId))
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