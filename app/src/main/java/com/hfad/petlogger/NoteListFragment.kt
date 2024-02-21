package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hfad.petlogger.databinding.FragmentNoteListBinding

class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        val view = binding.root

        val mainActivity = (activity as MainActivity)
        mainActivity.setTopAppBarTitle(getString(R.string.note_list_header))
        mainActivity.disableTopAppBarSubtitle()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}