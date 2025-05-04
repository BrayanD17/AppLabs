package com.labs.applabs.administrator

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R

class AdminAddFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_add_form)
        //updateStepIndicator(0) // Primer paso activo
        //updateStepIndicator(1) // Segundo paso activo
        //updateStepIndicator(2) // Tercer paso activo
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

    }
    private fun updateStepIndicator(currentStep: Int) {
        val steps = listOf(
            findViewById<TextView>(R.id.step1),
            findViewById<TextView>(R.id.step2),
            findViewById<TextView>(R.id.step3)
        )
        val lines = listOf(
            findViewById<View>(R.id.line1),
            findViewById<View>(R.id.line2)
        )

        for ((i, step) in steps.withIndex()) {
            step.setBackgroundResource(
                if (i <= currentStep) R.drawable.circle_selected else R.drawable.circle_unselected
            )
        }

        for ((i, line) in lines.withIndex()) {
            line.setBackgroundColor(
                if (i < currentStep) getColor(R.color.marineBlue) else getColor(R.color.gray)
            )
        }
    }


}