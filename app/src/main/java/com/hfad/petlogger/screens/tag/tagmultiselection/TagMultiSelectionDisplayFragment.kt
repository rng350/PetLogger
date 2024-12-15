package com.hfad.petlogger.screens.tag.tagmultiselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.hfad.petlogger.databinding.FragmentTagMultiSelectionDisplayBinding
import com.hfad.petlogger.screens.sections.recyclerviews.SetupTagMultiPickerSelectionDisplayUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TagMultiSelectionDisplayFragment : Fragment() {
    private var _binding: FragmentTagMultiSelectionDisplayBinding? = null
    val binding: FragmentTagMultiSelectionDisplayBinding get() = _binding!!
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment().requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTagMultiSelectionDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.addTagsButton.setOnClickListener {
            binding.addTagsButton.isEnabled = false
            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                TagMultiSelectionDialogFragment().show(childFragmentManager, "TAG_MULTI_PICKER")
                delay(200)
                binding.addTagsButton.isEnabled = true
            }
        }

        binding.resetButton.setOnClickListener {
            tagMultiSelectionViewModel.reset()
        }

        binding.tagList.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        SetupTagMultiPickerSelectionDisplayUseCase(
            selection = tagMultiSelectionViewModel.selectionTracker.visibleCurrentSelection,
            selectionTracker = tagMultiSelectionViewModel.selectionTracker,
            recyclerView = binding.tagList,
            lifecycleOwner = viewLifecycleOwner
        )()

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                tagMultiSelectionViewModel.onCurrentSelectionDisplayQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                tagMultiSelectionViewModel.onCurrentSelectionDisplayQueryTextChange(newText)
                return true
            }
        })

        return view
    }

    override fun onStop() {
        super.onStop()
        binding.addTagsButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.tagList.adapter = null
        _binding = null
    }
}