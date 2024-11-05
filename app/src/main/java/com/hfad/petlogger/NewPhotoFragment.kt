package com.hfad.petlogger

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.petlogger.databinding.FragmentNewNoteBinding
import com.hfad.petlogger.databinding.FragmentNewPhotoBinding
import com.hfad.petlogger.databinding.FragmentNewPhotoDetailsBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllNotesUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllTagsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllWeightsWithPetNamesUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.TagRepository
import com.hfad.petlogger.repositories.WeightRepository

class NewPhotoFragment : Fragment() {
    private var _binding: FragmentNewPhotoBinding? = null
    val binding get() = _binding!!
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPhotoBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val eventRepository = EventRepository(database, mediaRepository)

        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val newPhotoViewModel = ViewModelProvider(this, NewPhotoViewModel.provideFactory(mediaRepository)).get(NewPhotoViewModel::class.java)
        val petSelectorViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(getAllPetsUseCase)).get(PetMultiSelectionViewModel::class.java)
        val eventSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository)).get(EventMultiSelectionViewModel::class.java)

        val tagRepository = TagRepository(database)
        val getAllTags = GetAllTagsUseCase(tagRepository)
        val tagMultiSelectionViewModel = ViewModelProvider(this, TagMultiSelectionViewModel.provideFactory(tagRepository, getAllTags)).get(TagMultiSelectionViewModel::class.java)

        val noteRepository = NoteRepository(database, mediaRepository)
        val getAllNotes = GetAllNotesUseCase(noteRepository)
        val noteMultiSelectionViewModel = ViewModelProvider(this, NoteMultiSelectionViewModel.provideFactory(getAllNotes)).get(NoteMultiSelectionViewModel::class.java)

        setAppBarTitle(getString(R.string.new_photo_header))

        binding.viewPager.adapter = NewPhotoViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.details)
                1 -> getString(R.string.pets)
                2 -> getString(R.string.events)
                3 -> getString(R.string.notes)
                else -> null
            }
        }
        mediator?.attach()

        binding.newPhotoViewModel = newPhotoViewModel
        binding.petSelectorViewModel = petSelectorViewModel
        binding.eventSelectorViewModel = eventSelectionViewModel
        binding.noteMultiSelectionViewModel = noteMultiSelectionViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.clearButton.setOnClickListener{
            newPhotoViewModel.resetPhotoSelection()
        }

        binding.submitButton.setOnClickListener{
            newPhotoViewModel.submit(
                pets=petSelectorViewModel.getPetsToAdd(),
                events=eventSelectionViewModel.getEventsToAdd(),
                existingAttachedNotes = noteMultiSelectionViewModel.getNotesToAdd(),
                tags = tagMultiSelectionViewModel.getTagsToAdd()
            )
        }

        newPhotoViewModel.goBack.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigateSafe(NewPhotoFragmentDirections.actionNewPhotoFragmentToFullGalleryFragment())
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        _binding?.viewPager?.adapter = null
        _binding = null
    }

    private class NewPhotoViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> NewPhotoDetailsFragment()
                1 -> PetMultiSelectionDisplayFragment()
                2 -> EventMultiSelectionDisplayFragment()
                3 -> NoteMultiSelectionDisplayFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }
}

class NewPhotoDetailsFragment() : Fragment() {
    private var _binding: FragmentNewPhotoDetailsBinding? = null
    val binding: FragmentNewPhotoDetailsBinding get() = _binding!!
    private val newPhotoViewModel: NewPhotoViewModel by viewModels({requireParentFragment()})
    private val tagMultiSelectionViewModel: TagMultiSelectionViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPhotoDetailsBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.newPhotoViewModel = newPhotoViewModel
        binding.tagMultiSelectionViewModel = tagMultiSelectionViewModel

        newPhotoViewModel.photo.observe(viewLifecycleOwner) {
            if (it != null) {
                Glide.with(requireContext())
                    .load(it.contentUri)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(binding.photoDisplay)
            } else binding.photoDisplay.setImageResource(R.drawable.placeholder)
        }

        val pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    newPhotoViewModel.setPhoto(requireContext(), uri)
                }
            }

        val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                results ->
            if (        (Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                        && results[READ_MEDIA_IMAGES] == true
                        && results[READ_MEDIA_VISUAL_USER_SELECTED] == true)

                ||      (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU
                        && results[READ_MEDIA_IMAGES] == true)

                ||      (results[READ_EXTERNAL_STORAGE] == true)
            ) {
                launchImagePicker(pickSingleMedia)
            }
            else {
                makePermissionsRequiredToast()
            }
        }

        binding.selectPhotoButton.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if ((ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED)
                    || (ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED)) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED))
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES))
            }
            else {
                if (ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED) {
                    launchImagePicker(pickSingleMedia)
                }
                else requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
            }
        }

        binding.clearPhotoSelectionButton.setOnClickListener{
            newPhotoViewModel.resetPhotoSelection()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchImagePicker(pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>) {
        pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun makePermissionsRequiredToast() {
        Toast.makeText(requireContext(), "Can't read files without permission.", Toast.LENGTH_LONG).show()
    }
}