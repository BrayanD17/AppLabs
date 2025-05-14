package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.R
import com.labs.applabs.models.FormOperador
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

        val nombreFormulario = intent.getStringExtra("nombreFormulario") ?: ""
        val periodo = intent.getStringExtra("periodo") ?: ""
        val linkFormulario = intent.getStringExtra("linkFormulario") ?: ""

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

        val btnGuardar = findViewById<Button>(R.id.btnSaveForm)
        btnGuardar.setOnClickListener {
            val fechaHabilitado = tvHabilitado.text.toString().trim()
            val fechaCierre = tvCierre.text.toString().trim()
            val fechaCreacion = tvCreacion.text.toString().trim()

            if (fechaHabilitado.isEmpty() || fechaHabilitado == "dd/mm/yyyy") {
                Toast.makeText(this, "Por favor seleccione la fecha de habilitación.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaCierre.isEmpty() || fechaCierre == "dd/mm/yyyy") {
                Toast.makeText(this, "Por favor seleccione la fecha de cierre.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaCreacion.isEmpty() || fechaCreacion == "dd/mm/yyyy") {
                Toast.makeText(this, "La fecha de creación no es válida.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val sdf = dateFormat
                val fechaCreacionDate = sdf.parse(fechaCreacion)!!
                val fechaHabilitadoDate = sdf.parse(fechaHabilitado)!!
                val fechaCierreDate = sdf.parse(fechaCierre)!!

                val estadoFormulario = if (fechaHabilitadoDate.after(fechaCreacionDate)) 0 else 1

                val formulario = FormOperador(
                    activityStatus = estadoFormulario,
                    closingDate = Timestamp(fechaCierreDate),
                    createdDate = Timestamp(fechaCreacionDate),
                    nameForm = nombreFormulario,
                    semester = periodo,
                    startDate = Timestamp(fechaHabilitadoDate),
                    urlApplicationForm = linkFormulario,
                    year = Calendar.getInstance().get(Calendar.YEAR)
                )

                FirebaseFirestore.getInstance()
                    .collection("formOperator")
                    .add(formulario)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Formulario guardado exitosamente.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, AdminMenuFormActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            } catch (e: Exception) {
                Toast.makeText(this, "Error procesando fechas: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showMaterialDatePicker(title: String, onDateSelected: (String) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setTheme(R.style.CustomMaterialDatePickerTheme)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selection
            }
            val localCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            }
            onDateSelected(dateFormat.format(localCalendar.time))
        }
        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }
}
