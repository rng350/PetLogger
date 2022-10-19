package com.hfad.guineapiglog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel

class PetSingleSelectorDialogFragment<T>(var viewModel: T, var curSelectedPet: Pet? = null, var curSelectedIndex: Int = -1): DialogFragment() where T: ViewModel, T: WithSinglePetSelection {
    // could be NewWeightViewModel
    //private val viewModel: T by viewModels({requireParentFragment()})

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val pets = requireNotNull(viewModel.pets.value)
            val petNames = pets.map{ it.petName }.toTypedArray()

            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(R.string.select_pets)
                .setSingleChoiceItems(petNames, curSelectedIndex,
                    DialogInterface.OnClickListener { _, which ->
                        curSelectedPet = pets.get(which)
                        curSelectedIndex = which
                    })
                .setPositiveButton(R.string.add_pets,
                    DialogInterface.OnClickListener { _, _ ->
                        curSelectedPet?.let {
                            viewModel.petAssociated.value = it
                            Log.i("SELECTED", "Selected pet!: ${it.petName}")
                        }
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}