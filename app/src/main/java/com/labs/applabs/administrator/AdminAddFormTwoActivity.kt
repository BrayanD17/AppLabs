package com.labs.applabs.administrator

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.datepicker.MaterialDatePicker
import com.labs.applabs.R
import com.labs.applabs.utils.StepIndicatorActivity
import java.text.SimpleDateFormat
import java.util.*

class AdminAddFormTwoActivity : StepIndicatorActivity() {

    private lateinit var tvHabilitado: TextView
    private lateinit var tvCierre: TextView
    private lateinit var tvCreacion: TextView
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_form_two)

        updateStepIndicator(1)

        tvHabilitado = findViewById(R.id.tvHabilitado)
        tvCierre = findViewById(R.id.tvCierre)
        tvCreacion = findViewById(R.id.tvCreacion)

        // Fecha actual por defecto para creación
        tvCreacion.text = dateFormat.format(Calendar.getInstance().time)

        findViewById<ImageView>(R.id.ivHabilitado).setOnClickListener {
            showMaterialDatePicker("Seleccionar fecha de habilitación") { selectedDate ->
                tvHabilitado.text = selectedDate
            }
        }

        findViewById<ImageView>(R.id.ivCierre).setOnClickListener {
            showMaterialDatePicker("Seleccionar fecha de cierre") { selectedDate ->
                tvCierre.text = selectedDate
            }
        }
    }

    private fun showMaterialDatePicker(title: String, onDateSelected: (String) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setTheme(R.style.CustomMaterialDatePickerTheme)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply { timeInMillis = selection }
            onDateSelected(dateFormat.format(calendar.time))
        }

        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

}
