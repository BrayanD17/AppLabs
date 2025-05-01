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

    private companion object {
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

        // Restaurar datos si existen
        if (FormStudentData.idCard.isNotEmpty()) {
            etIdSchoolNumber.setText(FormStudentData.idCard)
        }
        if (FormStudentData.shift != 0) {
            shiftSelected = FormStudentData.shift
            val radioId = if (shiftSelected == 80) R.id.radioButton else R.id.radioButton2
            findViewById<RadioButton>(radioId).isChecked = true
        }
    }



    private fun updateShiftSelection() {
        shiftSelected = if (findViewById<RadioButton>(R.id.radioButton).isChecked) 80 else 160
    }

    private fun saveSchedules(): Boolean {
        val schedules = daysMap.mapNotNull { (day, checkboxIds) ->
            val shifts = mutableListOf<String>().apply {
                if (findViewById<CheckBox>(checkboxIds.first).isChecked) add("7am a 12pm")
                if (findViewById<CheckBox>(checkboxIds.second).isChecked) add("12pm a 5pm")
                if (findViewById<CheckBox>(checkboxIds.third).isChecked) add("5pm a 10pm")
            }
            if (shifts.isNotEmpty()) DaySchedule(day, shifts) else null
        }

        FormStudentData.schedule = schedules
        return schedules.isNotEmpty()
    }

    private fun validateFields(): Boolean {
        val idNumber = etIdSchoolNumber.text.toString().trim()

        if (idNumber.isEmpty()) {
            etIdSchoolNumber.error = getString(R.string.errorId)
            return false
        }

        try {
            idNumber.toInt()
        } catch (e: NumberFormatException) {
            etIdSchoolNumber.error = getString(R.string.errorNumber)
            return false
        }

        if (!findViewById<RadioButton>(R.id.radioButton).isChecked &&
            !findViewById<RadioButton>(R.id.radioButton2).isChecked) {
            Toast.makeText(this, R.string.errorHours, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveFormData(): Boolean {
        return try {
            FormStudentData.idCard = etIdSchoolNumber.text.toString()
            FormStudentData.shift = shiftSelected
            true
        } catch (e: Exception) {
            Toast.makeText(this, R.string.errorSavingData, Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun onShiftSelected(view: View) {
        updateShiftSelection()
    }

    fun Next(view: View) {
        if (validateFields() && saveSchedules() && saveFormData()) {
            startActivity(Intent(this, FormStudent3::class.java))
        } else if (!saveSchedules()) {
            Toast.makeText(this, R.string.errorSchedule, Toast.LENGTH_SHORT).show()
        }
    }

    fun Back(view: View) {
        finish()
    }
}