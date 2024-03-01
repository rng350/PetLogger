package com.hfad.petlogger

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.hfad.petlogger.entities.Pet

class PetMultiSelectorDialogFragment<T>: DialogFragment() where T: ViewModel, T: WithMultiPetSelection {
    // could be NewEventViewModel, NewNoteViewModel, NewPhotoViewModel
    private lateinit var viewModel: T

    companion object {
        fun <T> newInstance(viewModel: T): PetMultiSelectorDialogFragment<T> where T: ViewModel, T: WithMultiPetSelection {
            val instance = PetMultiSelectorDialogFragment<T>()
            instance.viewModel = viewModel
            return instance
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val pets = requireNotNull(viewModel.pets)
            val petNames = pets.map{ it.petName }.toTypedArray()

            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(R.string.select_pets)
                .setMultiChoiceItems(petNames, null,
                    DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
                        if (isChecked) {
                            selectedItems.add(which)
                        } else if (selectedItems.contains(which)) {
                            selectedItems.remove(which)
                        }
                    })
                .setPositiveButton(R.string.add_pets,
                    DialogInterface.OnClickListener { _, _ ->
                        val petsAssociated = mutableListOf<Pet>()
                        for (item in selectedItems) {
                            petsAssociated.add(pets[item])
                        }
                        viewModel.petsAssociated.value = petsAssociated
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setViewmodel(viewModel: T) {
        this.viewModel = viewModel
    }
}