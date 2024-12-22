package com.hfad.petlogger.screens.pet.newpet

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.common.DatePicker
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionDisplayFragment
import com.hfad.petlogger.screens.note.notemultiselection.NoteMultiSelectionViewModel
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.R
import com.hfad.petlogger.screens.tag.tagmultiselection.TagMultiSelectionViewModel
import com.hfad.petlogger.databinding.FragmentNewPetBinding
import com.hfad.petlogger.databinding.FragmentNewPetDetailsBinding
import com.hfad.petlogger.common.navigateSafe
import com.hfad.petlogger.tags.usecases.GetAllTagsUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionFragment
import com.hfad.petlogger.screens.photo.mediaselection.MediaSelectionViewModel
import com.hfad.petlogger.screens.photo.mediaselection.MediaSingleSelectionViewModel
import com.hfad.petlogger.common.setAppBarTitle
import com.hfad.petlogger.notes.usecases.GetAllNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.notes.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.usecases.GetMoreOfSearchedNotesUseCase
import com.hfad.petlogger.notes.usecases.GetSearchedNotesFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.usecases.GetAllTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.usecases.GetSearchedTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.usecases.GetSearchedTagsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NewPetFragment : Fragment() {
    private var _binding: FragmentNewPetBinding? = null
    private val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    lateinit var newPetViewModel: NewPetViewModel
    lateinit var profilePicSelectionViewModel: MediaSingleSelectionViewModel
    lateinit var photoMultiSelectionViewModel: MediaSelectionViewModel
    lateinit var noteMultiSelectionViewModel: NoteMultiSelectionViewModel
    lateinit var tagMultiSelectionViewModel: TagMultiSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        newPetViewModel = ViewModelProvider(this, NewPetViewModel.provideFactory(petRepository)).get(
            NewPetViewModel::class.java)
        binding.newPetViewModel = newPetViewModel

        profilePicSelectionViewModel = ViewModelProvider(this, MediaSingleSelectionViewModel.provideFactory(mediaRepository = mediaRepository)).get(
            MediaSingleSelectionViewModel::class.java)
        binding.petProfilePhotoSelectionViewModel = profilePicSelectionViewModel

        photoMultiSelectionViewModel = ViewModelProvider(this,
            MediaSelectionViewModel.provideFactory(
                mediaRepository = mediaRepository,
                maxItems = 10
            )
        ).get(MediaSelectionViewModel::class.java)
        binding.photoSelectionViewModel = photoMultiSelectionViewModel

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, noteAmt = 10)
        val getSearchedNotesFromAll = GetMoreOfSearchedNotesUseCase(database.noteDao, notesAmt = 10)
        val getAllNotesFromCurrentSelectionFactory = GetAllNotesFromCurrentSelectionUseCaseFactory()
        val getSearchedNotesFromCurrentSelectionFactory = GetSearchedNotesFromCurrentSelectionUseCaseFactory(database.noteDao)
        noteMultiSelectionViewModel = ViewModelProvider(this,
            NoteMultiSelectionViewModel.provideFactory(
                getAllNotes = getAllNotes,
                getSearchedSelectionOptions = getSearchedNotesFromAll,
                getAllNotesFromCurrentSelectionFactory = getAllNotesFromCurrentSelectionFactory,
                getSearchedNotesFromCurrentSelectionFactory = getSearchedNotesFromCurrentSelectionFactory
            )
        ).get(NoteMultiSelectionViewModel::class.java)
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val getAllSearchedTags = GetSearchedTagsUseCase(tagRepository)
        val getAllCurrentTagSelectionFactory = GetAllTagsFromCurrentSelectionUseCaseFactory()
        val getSearchedTagsFromCurrentSelectionUseCaseFactory = GetSearchedTagsFromCurrentSelectionUseCaseFactory(tagRepository)
        tagMultiSelectionViewModel = ViewModelProvider(this,
            TagMultiSelectionViewModel.provideFactory(
                getAllTags = getAllTags,
                getAllSearchedTagsUseCase = getAllSearchedTags,
                getAllCurrentSelectionFactory = getAllCurrentTagSelectionFactory,
                getSearchedTagsFromCurrentSelectionFactory = getSearchedTagsFromCurrentSelectionUseCaseFactory
            )
        ).get(TagMultiSelectionViewModel::class.java)
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        setAppBarTitle(getString(R.string.new_pet_header))

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = NewPetViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.notes)
                2 -> getString(R.string.photos_header)
                else -> null
            }
        }
        mediator?.attach()

        binding.submit.setOnClickListener {
            if (newPetViewModel.petName.isNotEmpty()) {
                newPetViewModel.addPet(
                    petProfilePhoto = profilePicSelectionViewModel.currentPhoto.value,
                    petPhotos = photoMultiSelectionViewModel.getPhotosToAdd(),
                    notes = noteMultiSelectionViewModel.getNotesToAdd(),
                    tags = tagMultiSelectionViewModel.getTagsToAdd()
                )
            } else Toast.makeText(requireContext(), R.string.no_pet_name_given, Toast.LENGTH_LONG).show()
        }

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clear.setOnClickListener{
            resetAll()
        }

        newPetViewModel.goToViewPet.observe(viewLifecycleOwner) {petId ->
            petId?.let {
                newPetViewModel.goToViewPet.value = null
                resetAll()
                findNavController().navigateSafe(NewPetFragmentDirections.actionNewPetFragmentToViewPetFragment(it))
            }
        }

        return view
    }

    private fun resetAll() {
        newPetViewModel.reset()
        profilePicSelectionViewModel.resetSelection()
        photoMultiSelectionViewModel.resetSelection()
        noteMultiSelectionViewModel.resetSelection()
        tagMultiSelectionViewModel.reset()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }

    private class NewPetViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> NewPetDetailsFragment()
                1 -> NoteMultiSelectionDisplayFragment()
                2 -> MediaSelectionFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class NewPetDetailsFragment() : Fragment() {
    private var _binding: FragmentNewPetDetailsBinding? = null
    val binding: FragmentNewPetDetailsBinding get() = _binding!!
    private val newPetViewModel: NewPetViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPetDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        binding.newPetViewModel = newPetViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.petSexSelection.setOnCheckedChangeListener { radioGroup, i ->
            when(binding.petSexSelection.checkedRadioButtonId) {
                binding.petSexMale.id -> newPetViewModel.setPetSex("Male")
                binding.petSexFemale.id -> newPetViewModel.setPetSex("Female")
                binding.petSexOther.id -> newPetViewModel.setPetSex("Other")
                -1 -> newPetViewModel.setPetSex("")
            }
            Log.d("pet_sex_selection", "${binding.petSexSelection.checkedRadioButtonId} : ${newPetViewModel.petSex}")
        }
        binding.addPetBirthDateButton.isEnabled = true
        binding.addPetBirthDateButton.setOnClickListener {
            binding.addPetBirthDateButton.isEnabled = false
            CoroutineScope(Dispatchers.Main.immediate).launch {
                DatePicker.generate(newPetViewModel.petDOB)
                    .show(parentFragmentManager, "DATE_PICKER")
                delay(200)
                binding.addPetBirthDateButton.isEnabled = true
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}