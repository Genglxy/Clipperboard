package com.genglxy.clipperboard.ui.clipper

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HeightPickerFragment : DialogFragment() {
    private val args: HeightPickerFragmentArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val fixedHeight = ArrayList<String>()
        var selectIndex = args.index
        for (height in args.fixedHeight) {
            fixedHeight.add("$height Pixels")
        }
        return MaterialAlertDialogBuilder(requireContext()).setTitle("Optional height")
            //.setMessage("Select default height")
            .setSingleChoiceItems(fixedHeight.toTypedArray(), selectIndex) { _, which ->
                selectIndex = which
            }.setNegativeButton("Cancel") { _, _ ->
                // Respond to negative button press
            }.setPositiveButton("Apply") { _, _ ->
                setFragmentResult(
                    REQUEST_KEY_INDEX, bundleOf(
                        BUNDLE_KEY_INDEX to selectIndex
                    )
                )
            }.show()
    }

    companion object {
        const val REQUEST_KEY_INDEX = "REQUEST_KEY_INDEX"
        const val BUNDLE_KEY_INDEX = "BUNDLE_KEY_INDEX"
    }
}