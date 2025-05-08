package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R
import com.labs.applabs.utils.StepIndicatorActivity

class AdminAddFormActivity : StepIndicatorActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_form)

        updateStepIndicator(0)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupPeriodo)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonISemestre -> {
                    // Acción para I Semestre
                }
                R.id.radioButtonIISemestre -> {
                    // Acción para II Semestre
                }
                R.id.radioButtonVerano -> {
                    // Acción para Verano
                }
            }
        }
        val btnSiguiente = findViewById<View>(R.id.btnSiguiente1)
        btnSiguiente.setOnClickListener {
            val intent = Intent(this, AdminAddFormTwoActivity::class.java)
            startActivity(intent)
        }

    }
}