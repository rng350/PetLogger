package com.hfad.petlogger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.hfad.petlogger.databinding.FragmentViewPetBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hfad.petlogger.photodisplay.stateful.GetEventsOfPetForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateful.GetPhotosOfPetForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateful.GetWeightsOfPetForDisplayUseCase
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.util.GetPeriodDisplayUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewPetFragment : Fragment() {

    private var _binding: FragmentViewPetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPetBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.lifecycleOwner = viewLifecycleOwner

        val application = requireNotNull(this.activity).application
        val database = PetLoggerDatabase.getInstance(application)
        val petId = ViewPetFragmentArgs.fromBundle(requireArguments()).petId
        val mediaRepository = MediaRepository(database, application.applicationContext)
        val petRepository = PetRepository(database, mediaRepository)
        val getPetAgeDisplay = GetPeriodDisplayUseCase()
        val viewPetViewModel = ViewModelProvider(this, ViewPetViewModel.provideFactory(petRepository, petId, getPetAgeDisplay)).get(ViewPetViewModel::class.java)
        binding.viewPetViewModel = viewPetViewModel

        val getAssociatedEvents = GetEventsOfPetForDisplayUseCase(petRepository, petId)
        val associatedEventsDisplayViewModel = ViewModelProvider(this, AssociatedEventsDisplayViewModel.provideFactory(getAssociatedEvents)).get(AssociatedEventsDisplayViewModel::class.java)
        binding.associatedEventsDisplayViewModel = associatedEventsDisplayViewModel

        val getAssociatedWeights = GetWeightsOfPetForDisplayUseCase(petRepository, petId)
        val associatedWeightsDisplayViewModel = ViewModelProvider(this, AssociatedPetWeightsDisplayViewModel.provideFactory(getAssociatedWeights)).get(AssociatedPetWeightsDisplayViewModel::class.java)
        binding.associatedPetWeightsDisplayViewModel = associatedWeightsDisplayViewModel

        val getPhotosOfPetForDisplayUseCase = GetPhotosOfPetForDisplayUseCase(petRepository, petId)
        val associatedPhotosDisplayViewModel = ViewModelProvider(this, AssociatedPhotosDisplayViewModel.provideFactory(getPhotosOfPetForDisplayUseCase)).get(AssociatedPhotosDisplayViewModel::class.java)
        binding.associatedPhotosDisplayViewModel = associatedPhotosDisplayViewModel

        viewPetViewModel.pet.observe(viewLifecycleOwner, Observer {
            it?.let {
                setAppBarTitle(it.petName, getString(R.string.viewing_details))
            }
        })

        viewPetViewModel.petProfilePhoto.observe(viewLifecycleOwner, Observer { it ->
            Glide.with(requireContext())
                .load(it.contentUri)
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(binding.petPhoto)
        })

        associatedEventsDisplayViewModel.eventNavigator.navigateTo.observe(viewLifecycleOwner) { eventId ->
            eventId?.let {
                associatedEventsDisplayViewModel.eventNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewEventFragment(eventId))
            }
        }

        associatedPhotosDisplayViewModel.navigator.navigateTo.observe(viewLifecycleOwner) {photoId ->
            photoId?.let {
                associatedPhotosDisplayViewModel.navigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewPhotoFragment(photoId))
            }
        }

        associatedWeightsDisplayViewModel.weightNavigator.navigateTo.observe(viewLifecycleOwner) {weightId ->
            weightId?.let {
                associatedWeightsDisplayViewModel.weightNavigator.onNavigated()
                findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToViewWeightFragment(weightId))
            }
        }

        binding.editPetButton.setOnClickListener {
            findNavController().navigateSafe(ViewPetFragmentDirections.actionViewPetFragmentToEditPetFragment(petId))
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                associatedWeightsDisplayViewModel.weights.collectLatest{petWeights ->
                    viewPetViewModel.setLatestWeight(petWeights)
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}