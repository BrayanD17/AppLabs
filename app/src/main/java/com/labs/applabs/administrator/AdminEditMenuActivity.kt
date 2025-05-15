package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import com.labs.applabs.models.FormOperador
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class AdminEditMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_menu)

        val nameForm = intent.getStringExtra("nameForm") ?: return
        val semester = intent.getStringExtra("semester") ?: return
        val year = intent.getIntExtra("year", -1)
        val provider = Provider()

        // EditTexts
        val etNameForm = findViewById<EditText>(R.id.et_name_edit_form)
        val etLinkForm = findViewById<EditText>(R.id.et_link_edit_form)

        // RadioButtons
        val rbtnISemestre = findViewById<RadioButton>(R.id.rbtnISemestreEdit)
        val rbtnIISemestre = findViewById<RadioButton>(R.id.rbtnIISemestreEdit)
        val rbtnVerano = findViewById<RadioButton>(R.id.rbtnVeranoEdit)

        // TextViews de fechas y año
        val tvDateHability = findViewById<TextView>(R.id.tvDateHabilityEdit)
        val tvDateDishability = findViewById<TextView>(R.id.tvDateDishabilityEdit)
        val tvYear = findViewById<TextView>(R.id.tvYear)

        // ChipGroup
        val chipGroupStatus = findViewById<ChipGroup>(R.id.chipGroupStatus)
        val chipHabilitar = findViewById<Chip>(R.id.chipHabilitar)
        val chipDeshabilitar = findViewById<Chip>(R.id.chipDeshabilitar)


        lifecycleScope.launch {
            val formulario = provider.getFormOperator(nameForm, semester, year)

            if (formulario != null) {
                Toast.makeText(this@AdminEditMenuActivity, "Formulario: ${formulario.nameForm}", Toast.LENGTH_SHORT).show()
                formulario?.let { form ->

                    // Nombre del formulario
                    etNameForm.setText(form.nameForm)

                    // Link del formulario
                    etLinkForm.setText(form.urlApplicationForm)

                    // Periodo seleccionado
                    when (form.semester.trim().lowercase()) {
                        "i semestre" -> rbtnISemestre.isChecked = true
                        "ii semestre" -> rbtnIISemestre.isChecked = true
                        "verano" -> rbtnVerano.isChecked = true
                    }

                    // Fechas (formato dd/MM/yyyy)
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    form.startDate?.let {
                        tvDateHability.text = sdf.format(it.toDate())
                    }
                    form.closingDate?.let {
                        tvDateDishability.text = sdf.format(it.toDate())
                    }

                    // Año
                    tvYear.text = form.year.toString()

                    // Estado (0: Deshabilitado, 1: Habilitado)
                    when (form.activityStatus) {
                        0 -> {
                            chipDeshabilitar.isChecked = true
                            chipDeshabilitar.setChipBackgroundColorResource(R.color.green)
                            chipHabilitar.setChipBackgroundColorResource(R.color.marineBlue)
                        }
                        1 -> {
                            chipHabilitar.isChecked = true
                            chipHabilitar.setChipBackgroundColorResource(R.color.green)
                            chipDeshabilitar.setChipBackgroundColorResource(R.color.marineBlue)
                        }
                    }

                }

            } else {
                Toast.makeText(this@AdminEditMenuActivity, "Formulario no encontrado", Toast.LENGTH_LONG).show()
            }
        }
        val ivDateHability = findViewById<ImageView>(R.id.ivDateHability)
        val ivDateDishability = findViewById<ImageView>(R.id.ivDateDishability)
        val ivYear = findViewById<ImageView>(R.id.ivYear)

        ivDateHability.setOnClickListener {
            showMaterialDatePicker("Seleccionar fecha de habilitación") { selectedDate ->
                tvDateHability.text = selectedDate
            }
        }

        ivDateDishability.setOnClickListener {
            showMaterialDatePicker("Seleccionar fecha de cierre") { selectedDate ->
                tvDateDishability.text = selectedDate
            }
        }

        ivYear.setOnClickListener {
            showYearPickerDialog { selectedYear ->
                tvYear.text = selectedYear.toString()
            }
        }
        chipGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipHabilitar -> {
                    chipHabilitar.setChipBackgroundColorResource(R.color.green)
                    chipDeshabilitar.setChipBackgroundColorResource(R.color.marineBlue)
                }
                R.id.chipDeshabilitar -> {
                    chipDeshabilitar.setChipBackgroundColorResource(R.color.green)
                    chipHabilitar.setChipBackgroundColorResource(R.color.marineBlue)
                }
            }
        }
        val btnSave = findViewById<Button>(R.id.btnSaveEdit)
        btnSave.setOnClickListener {
            val updatedName = etNameForm.text.toString().trim()
            val updatedLink = etLinkForm.text.toString().trim()
            val updatedSemester = when {
                rbtnISemestre.isChecked -> "I semestre"
                rbtnIISemestre.isChecked -> "II semestre"
                rbtnVerano.isChecked -> "Verano"
                else -> ""
            }
            val updatedStartDate = tvDateHability.text.toString().trim()
            val updatedEndDate = tvDateDishability.text.toString().trim()
            val updatedYear = tvYear.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            val updatedStatus = if (chipHabilitar.isChecked) 1 else 0

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val startTimestamp = com.google.firebase.Timestamp(sdf.parse(updatedStartDate)!!)
                val endTimestamp = com.google.firebase.Timestamp(sdf.parse(updatedEndDate)!!)

                val updatedMap = mapOf(
                    "nameForm" to updatedName,
                    "urlApplicationForm" to updatedLink,
                    "semester" to updatedSemester,
                    "startDate" to startTimestamp,
                    "closingDate" to endTimestamp,
                    "year" to updatedYear,
                    "activityStatus" to updatedStatus
                )

                lifecycleScope.launch {
                    provider.updateFormOperator(
                        nameForm = nameForm,
                        semester = semester,
                        year = year,
                        updatedData = updatedMap,
                        onSuccess = {
                            Toast.makeText(this@AdminEditMenuActivity, "Formulario actualizado exitosamente.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AdminEditMenuActivity, AdminMenuFormActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        ,
                        onFailure = { e ->
                            Toast.makeText(this@AdminEditMenuActivity, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Fechas inválidas: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun showMaterialDatePicker(title: String, onDateSelected: (String) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setTheme(R.style.CustomMaterialDatePickerTheme) // usa tu tema si ya lo tienes definido
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
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            onDateSelected(dateFormat.format(localCalendar.time))
        }

        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun showYearPickerDialog(onYearSelected: (Int) -> Unit) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val numberPicker = NumberPicker(this).apply {
            minValue = 2000
            maxValue = currentYear + 10
            value = currentYear
            wrapSelectorWheel = false
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Seleccionar año")
            .setView(numberPicker)
            .setPositiveButton("Aceptar") { _, _ ->
                onYearSelected(numberPicker.value)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }


}