package com.hfad.petlogger

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import com.hfad.petlogger.entities.Pet

class PetSingleSelectorDialogFragment<T>(): DialogFragment() where T: ViewModel, T: WithSinglePetSelection {
    // could be NewWeightViewModel
    private lateinit var viewModel: T
    private var curSelectedPet: Pet? = null
    private var curSelectedIndex: Int = -1

    companion object {
        fun <T> newInstance(vm: T, selectedPet: Pet? = null, selectedIndex: Int = -1): PetSingleSelectorDialogFragment<T>
                where T: ViewModel, T: WithSinglePetSelection {
            Log.e("pet_selector", "created new instance!")
            val instance = PetSingleSelectorDialogFragment<T>()
            instance.setupDialogFragment(vm, selectedPet, selectedIndex)
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    fun setupDialogFragment(vm: T, selectedPet: Pet? = null, selectedIndex: Int = -1) {
        this.viewModel = vm
        this.curSelectedPet = selectedPet
        this.curSelectedIndex = selectedIndex
    }
}