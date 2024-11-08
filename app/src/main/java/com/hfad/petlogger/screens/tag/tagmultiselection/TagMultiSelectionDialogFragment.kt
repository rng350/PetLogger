package com.hfad.petlogger.screens.tag.tagmultiselection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.hfad.petlogger.databinding.FragmentTagMultiSelectionDialogBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupTagMultiPickerUseCase

class TagMultiSelectionDialogFragment : DialogFragment() {
    private var _binding: FragmentTagMultiSelectionDialogBinding? = null
    val binding: FragmentTagMultiSelectionDialogBinding get() = _binding!!
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment().requireParentFragment().requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTagMultiSelectionDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.confirmButton.setOnClickListener {
            tagMultiSelectionViewModel.confirmSelection()
            requireDialog().dismiss()
        }

        binding.cancelButton.setOnClickListener {
            requireDialog().cancel()
        }

        binding.tagsList.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        SetupTagMultiPickerUseCase(
            tagList = tagMultiSelectionViewModel.selectionTracker.visibleOptions,
            selection = tagMultiSelectionViewModel.selectionTracker.prospectiveSelection,
            selectionTracker = tagMultiSelectionViewModel.selectionTracker,
            recyclerView = binding.tagsList,
            lifecycleOwner = viewLifecycleOwner
        )()

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                tagMultiSelectionViewModel.onQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                tagMultiSelectionViewModel.onQueryTextChanged(newText)
                return true
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onStop() {
        super.onStop()
        Log.d("PetSingleDialog", "onStop called")
        if (tagMultiSelectionViewModel.currentSelectionChanged) {
            tagMultiSelectionViewModel.onCurrentSelectionChanged()
        } else {
            tagMultiSelectionViewModel.cancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.tagsList.adapter = null
        _binding = null
    }
}