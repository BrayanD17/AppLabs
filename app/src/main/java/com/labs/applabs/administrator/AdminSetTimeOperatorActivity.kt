package com.labs.applabs.administrator

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R

class AdminSetTimeOperatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_set_time_operator)
        val infoIcon: ImageView = findViewById(R.id.infoIcon)
        infoIcon.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Horarios de turnos")
                .setMessage(
                    "Mañana: 6:00 am - 12:00 md\n" +
                            "Tarde: 12:00 md - 6:00 pm\n" +
                            "Noche: 6:00 pm - 10:00 pm"
                )
                .setPositiveButton("Entendido", null)
                .show()
        }

    }
}