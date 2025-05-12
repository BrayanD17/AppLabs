package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.labs.applabs.R
import com.labs.applabs.utils.StepIndicatorActivity

class AdminAddFormActivity : StepIndicatorActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_form)

        updateStepIndicator(0)

        val etNombreFormulario = findViewById<TextView>(R.id.et_name_add_form)
        val etLinkFormulario = findViewById<TextView>(R.id.et_link_form)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupPeriodo)
        val btnSiguiente1 = findViewById<Button>(R.id.btnSiguiente1)

        btnSiguiente1.setOnClickListener {
            val nombreFormulario = etNombreFormulario.text.toString().trim()
            val linkFormulario = etLinkFormulario.text.toString().trim()

            // Validaciones de campos vacíos
            if (nombreFormulario.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese el nombre del formulario.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (linkFormulario.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese el link del formulario.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de selección de periodo
            val periodoSeleccionadoId = radioGroup.checkedRadioButtonId
            val periodoSeleccionado = when (periodoSeleccionadoId) {
                R.id.radioButtonISemestre -> "I semestre"
                R.id.radioButtonIISemestre -> "II semestre"
                R.id.radioButtonVerano -> "Verano"
                else -> {
                    Toast.makeText(this, "Por favor seleccione un período.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Si todo es válido, pasar a la siguiente Activity
            val intent = Intent(this, AdminAddFormTwoActivity::class.java)
            intent.putExtra("nombreFormulario", nombreFormulario)
            intent.putExtra("periodo", periodoSeleccionado)
            intent.putExtra("linkFormulario", linkFormulario)
            startActivity(intent)
        }
    }
}
