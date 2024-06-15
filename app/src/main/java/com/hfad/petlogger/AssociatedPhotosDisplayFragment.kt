package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.databinding.FragmentAssociatedPhotosDisplayBinding
import com.hfad.petlogger.recyclerviews.SetupAssociatedPhotosDisplayUseCase
import kotlinx.coroutines.launch

class AssociatedPhotosDisplayFragment : Fragment() {
    var _binding: FragmentAssociatedPhotosDisplayBinding? = null
    val binding: FragmentAssociatedPhotosDisplayBinding get() = _binding!!
    private val assocPhotosViewModel: AssociatedPhotosDisplayViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssociatedPhotosDisplayBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        binding.associatedPhotosDisplayViewModel = assocPhotosViewModel

        SetupAssociatedPhotosDisplayUseCase(
            assocPhotosViewModel.photos,
            assocPhotosViewModel.navigator,
            binding.photoList,
            requireContext(),
            lifecycleScope,
            viewLifecycleOwner
        )()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}