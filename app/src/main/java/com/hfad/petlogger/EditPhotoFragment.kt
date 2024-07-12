package com.hfad.petlogger

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.hfad.petlogger.databinding.FragmentEditPhotoBinding
import com.hfad.petlogger.photodisplay.stateless.GetAllPetsWithProfilePhotosUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllWeightsWithPetNamesUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.repositories.WeightRepository

class EditPhotoFragment : Fragment() {

    private var _binding: FragmentEditPhotoBinding? = null
    private val binding get() = _binding!!
    private lateinit var editPhotoViewModel: EditPhotoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPhotoBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val photoId = EditPhotoFragmentArgs.fromBundle(requireArguments()).photoId

        val noteRepository = NoteRepository(database, mediaRepository)
        val petRepository = PetRepository(database, mediaRepository)
        val eventRepository = EventRepository(database, mediaRepository)
        val weightRepository = WeightRepository(database)

        val getAllPetsUseCase = GetAllPetsWithProfilePhotosUseCase(petRepository)
        val petSelectorViewModel = ViewModelProvider(this, PetMultiSelectionViewModel.provideFactory(getAllPetsUseCase)).get(PetMultiSelectionViewModel::class.java)
        val eventSelectionViewModel = ViewModelProvider(this, EventMultiSelectionViewModel.provideFactory(eventRepository)).get(EventMultiSelectionViewModel::class.java)

        val getAllWeights = GetAllWeightsWithPetNamesUseCase(weightRepository)
        val weightMultiSelectionViewModel = ViewModelProvider(this, WeightMultiSelectionViewModel.provideFactory(getAllWeights)).get(WeightMultiSelectionViewModel::class.java)

        editPhotoViewModel = ViewModelProvider(this, EditPhotoViewModel.provideFactory(mediaRepository, photoId)).get(EditPhotoViewModel::class.java)
        binding.editPhotoViewModel = editPhotoViewModel
        binding.petSelectorViewModel = petSelectorViewModel
        binding.eventSelectorViewModel = eventSelectionViewModel
        binding.weightSelectorViewModel = weightMultiSelectionViewModel

        setAppBarTitle(getString(R.string.editing_photo_details))

        binding.submitButton.setOnClickListener{
            editPhotoViewModel.submit(
                petsToAdd = petSelectorViewModel.getPetsToAdd(),
                petsToRemove = petSelectorViewModel.getPetsToRemove(),
                eventsToAdd = eventSelectionViewModel.getEventsToAdd(),
                eventsToRemove = eventSelectionViewModel.getEventsToRemove(),
                weightsToAdd = weightMultiSelectionViewModel.getWeightsToAdd(),
                weightsToRemove = weightMultiSelectionViewModel.getWeightsToRemove()
            )
        }

        val confirmAction = ConfirmActionUseCase(
            dialogTitle = resources.getString(R.string.confirm_photo_deletion_title),
            dialogMessage = resources.getString(R.string.confirm_photo_deletion_message),
            onPositiveButtonClick = { dialog, which ->
                dialog.dismiss()
                editPhotoViewModel.deletePhoto() },
            context = requireContext()
        )
        binding.deleteButton.setOnClickListener{
            confirmAction()
        }

        binding.resetButton.setOnClickListener{
            editPhotoViewModel.reset()
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        editPhotoViewModel.goBack.observe(viewLifecycleOwner, Observer {shouldGo ->
            if (shouldGo) {
                findNavController().navigateSafe(EditPhotoFragmentDirections.actionEditPhotoFragmentToViewPhotoFragment(photoId))
            }
        })

        editPhotoViewModel.goToGalleryList.observe(viewLifecycleOwner, Observer { shouldGo ->
            if (shouldGo) {
                editPhotoViewModel.onNavigateToGalleryList()
                findNavController().navigateSafe(EditPhotoFragmentDirections.actionEditPhotoFragmentToFullGalleryFragment())
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}