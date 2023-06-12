package com.genglxy.clipperboard.ui.clipper

import android.app.Dialog
import android.icu.text.MessageFormat
import android.os.Build
import android.os.Bundle
import android.os.Message
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class DeleteDialogFragment : DialogFragment() {
    private val args: DeleteDialogFragmentArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val position = args.position
        val locale = Locale.getDefault()
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MessageFormat("{0,ordinal}", Locale.US)
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        return MaterialAlertDialogBuilder(requireContext()).setTitle("Delete?")
            .setMessage("About to delete the ${formatter.format(arrayOf(position + 1))} picture.")
            .setNegativeButton("Not now") { _, _ ->
                // Respond to negative button press
            }.setPositiveButton("Sure") { _, _ ->
                setFragmentResult(
                    REQUEST_KEY_DELETE, bundleOf(
                        BUNDLE_KEY_DELETE to position
                    )
                )
            }.show()
    }
    companion object {
        const val REQUEST_KEY_DELETE = "REQUEST_KEY_DELETE"
        const val BUNDLE_KEY_DELETE = "BUNDLE_KEY_DELETE"
    }
}