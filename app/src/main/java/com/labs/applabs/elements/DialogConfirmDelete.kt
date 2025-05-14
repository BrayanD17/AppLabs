package com.labs.applabs.elements

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import com.labs.applabs.R

fun Context.dialogConfirmDelete(onConfirm: () -> Unit) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null)
    val builder = AlertDialog.Builder(this)
        .setView(dialogView)

    val dialog = builder.create()

    val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
    val btnAccept = dialogView.findViewById<Button>(R.id.btnAccept)

    btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    btnAccept.setOnClickListener {
        dialog.dismiss()
        onConfirm()
    }

    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
}
