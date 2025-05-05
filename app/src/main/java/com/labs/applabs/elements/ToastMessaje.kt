package com.labs.applabs.elements

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.labs.applabs.R

enum class ToastType {
    SUCCESS,
    ERROR
}

fun Context.toastMessage(message: String, type: ToastType) {
    val inflater = LayoutInflater.from(this)
    val layout = inflater.inflate(R.layout.toast_message, null)

    val text: TextView = layout.findViewById(R.id.toast_text)
    val icon: ImageView = layout.findViewById(R.id.toast_icon)
    val container: LinearLayout = layout as LinearLayout
    val typeLetter = ResourcesCompat.getFont(this, R.font.montserrat_regular)

    text.text = message
    text.typeface = typeLetter

    when (type) {
        ToastType.SUCCESS -> {
            icon.setImageResource(R.drawable.check_circle)
            container.setBackgroundResource(R.drawable.rectangule_toast)
        }
        ToastType.ERROR -> {
            icon.setImageResource(R.drawable.error_circle)
            container.setBackgroundResource(R.drawable.rectangule_toast)
        }
    }

    val toast = Toast(this)
    toast.duration = Toast.LENGTH_SHORT
    toast.view = layout
    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
    toast.show()
}
