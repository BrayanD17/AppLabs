package com.labs.applabs.student

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R

class FormStudent2 : AppCompatActivity() {

    private lateinit var etIdSchoolNumber: EditText
    private var shiftSelected: Int = 0

    companion object {
        private val daysMap = mapOf(
            "Lunes" to Triple(R.id.lunes_m, R.id.lunes_t, R.id.lunes_n),
            "Martes" to Triple(R.id.martes_m, R.id.martes_t, R.id.martes_n),
            "Miércoles" to Triple(R.id.miercoles_m, R.id.miercoles_t, R.id.miercoles_n),
            "Jueves" to Triple(R.id.jueves_m, R.id.jueves_t, R.id.jueves_n),
            "Viernes" to Triple(R.id.viernes_m, R.id.viernes_t, R.id.viernes_n),
            "Sábado" to Triple(R.id.sabado_m, R.id.sabado_t, R.id.sabado_n),
            "Domingo" to Triple(R.id.domingo_m, R.id.domingo_t, R.id.domingo_n)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_student2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etIdSchoolNumber = findViewById(R.id.etIdSchoolNumber)
        updateShiftSelection()

        // Rellenar si hay datos guardados
        if (FormStudentData.idCard.isNotEmpty()) {
            etIdSchoolNumber.setText(FormStudentData.idCard)
        }
    }

    fun onShiftSelected(view: View) {
        updateShiftSelection()
    }

    private fun updateShiftSelection() {
        shiftSelected = if (findViewById<RadioButton>(R.id.radioButton).isChecked) 80 else 160
    }

    private fun saveSchedules() {
        FormStudentData.schedule = daysMap.map { (day, checkboxIds) ->
            val shifts = mutableListOf<String>().apply {
                if (findViewById<CheckBox>(checkboxIds.first).isChecked) add("7am a 12pm")
                if (findViewById<CheckBox>(checkboxIds.second).isChecked) add("12pm a 5pm")
                if (findViewById<CheckBox>(checkboxIds.third).isChecked) add("5pm a 10pm")
            }
            DaySchedule(day, shifts)
        }.filter { it.shifts.isNotEmpty() }
    }


    private fun validateFields(): Boolean {
        if (etIdSchoolNumber == null || etIdSchoolNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Los dígitos del carnet son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }

        if (FormStudentData.schedule.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un horario", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveFormData() {
        try {
            FormStudentData.IdSchoolNumber = etIdSchoolNumber.text.toString().toInt()
            FormStudentData.shift = shiftSelected
            saveSchedules()
        } catch (e: NumberFormatException) {
            etIdSchoolNumber.error = "Número inválido"
            throw e
        }
    }

    fun Next(view: View) {
        if (validateFields()) {
            saveFormData()
            startActivity(Intent(this, FormStudent3::class.java))
        }
    }

    fun Back(view: View) {
        finish()
    }
}