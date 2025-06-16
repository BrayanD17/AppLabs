package com.labs.applabs.operator

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.LabSchedule
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class viewAssignedSchedule : AppCompatActivity() {

    private val provider: Provider = Provider()
    val colors = listOf("#FF6F61", "#6B5B95", "#88B04B", "#F7CAC9", "#92A8D1")

    private companion object {
        private val daysMap = mapOf(
            "Lunes" to Triple(R.id.DataMondayMorning, R.id.DataMondayAfternoon, R.id.DataMondayEvening),
            "Martes" to Triple(R.id.DataTuesdayMorning, R.id.DataTuesdayAfternoon, R.id.DataTuesdayEvening),
            "Miércoles" to Triple(R.id.DataWednesdayMorning, R.id.DataWednesdayAfternoon, R.id.DataWednesdayEvening),
            "Jueves" to Triple(R.id.DataThursdayMorning, R.id.DataThursdayAfternoon, R.id.DataThursdayEvening),
            "Viernes" to Triple(R.id.DataFridayMorning, R.id.DataFridayAfternoon, R.id.DataFridayEvening),
            "Sábado" to Triple(R.id.DataSaturdayMorning, R.id.DataSaturdayAfternoon, R.id.DataSaturdayEvening),
            "Domingo" to Triple(R.id.DataSundayMorning, R.id.DataSundayAfternoon, R.id.DataSundayEvening)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_assigned_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        displayAssignedLabs()

        lifecycleScope.launch {
            try {
                val scheduleData = provider.getAssignedSchedule()
                if (scheduleData != null) {
                    val labSchedules = parseScheduleData(scheduleData)
                    displayScheduleTable(labSchedules)
                } else {
                    Log.d("viewAssignedSchedule", "No se encontraron horarios asignados")
                }
            } catch (e: Exception) {
                Log.e("viewAssignedSchedule", "Error al cargar horarios: ${e.message}")
            }
        }

        findViewById<ConstraintLayout>(R.id.main).setOnClickListener {
            lifecycleScope.launch {
                val scheduleData = provider.getAssignedSchedule()
                if (scheduleData != null) {
                    val labSchedules = parseScheduleData(scheduleData)
                    restoreAllColors(labSchedules)
                }
            }
        }
    }

    private fun parseScheduleData(data: Map<String, Any>?): List<LabSchedule> {
        val labSchedules = mutableListOf<LabSchedule>()

        data?.let { scheduleData ->
            val labsMap = scheduleData["labs"] as? Map<String, Map<String, Any>> ?: return labSchedules

            labsMap.forEach { (labName, daysMap) ->
                val days = mutableMapOf<String, List<String>>()

                daysMap.forEach { (day, shifts) ->
                    val shiftsList = when (shifts) {
                        is String -> listOf(shifts)
                        is List<*> -> shifts.filterIsInstance<String>()
                        else -> emptyList()
                    }
                    days[day] = shiftsList
                }
                labSchedules.add(LabSchedule(labName, days))
            }
        }

        return labSchedules
    }

    private fun displayAssignedLabs() {
        //Add data from checkboxes
        lifecycleScope.launch {
            val laboratoryNames = provider.getLaboratoryName()

            val container = findViewById<LinearLayout>(R.id.containerCheckBoxes)
            container.removeAllViews()

            laboratoryNames.forEachIndexed { index, name ->
                val color = colors.getOrElse(index) { "#000000" } // negro por defecto si faltan colores
                val checkBox = CheckBox(this@viewAssignedSchedule).apply {
                    text = name
                    isChecked = true
                    isClickable = false
                    textAlignment= LinearLayout.TEXT_ALIGNMENT_TEXT_START
                    setTextAppearance(this@viewAssignedSchedule, R.style.formMessageStyle)
                    buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,

                        ).apply {
                        setMargins(8, 8, 8, 8)
                    }
                }
                container.addView(checkBox)
            }
        }
    }

    private fun displayScheduleTable(labSchedules: List<LabSchedule>) {
        daysMap.values.forEach { (morningId, afternoonId, eveningId) ->
            resetCheckBox(morningId)
            resetCheckBox(afternoonId)
            resetCheckBox(eveningId)
        }

        labSchedules.forEachIndexed { labIndex, labSchedule ->
            val color = colors.getOrElse(labIndex % colors.size) { "#000000" }

            labSchedule.days.forEach { (dayName, shifts) ->
                val dayKey = when (dayName.lowercase()) {
                    "lunes" -> "Lunes"
                    "martes" -> "Martes"
                    "miercoles", "miércoles" -> "Miércoles"
                    "jueves" -> "Jueves"
                    "viernes" -> "Viernes"
                    "sabado", "sábado" -> "Sábado"
                    "domingo" -> "Domingo"
                    else -> dayName
                }

                val (morningId, afternoonId, eveningId) = daysMap[dayKey] ?: return@forEach

                shifts.forEach { shift ->
                    when {
                        shift.contains("mañana", ignoreCase = true) ->
                            setCheckBoxColor(morningId, color, labSchedule.labName)
                        shift.contains("tarde", ignoreCase = true) ->
                            setCheckBoxColor(afternoonId, color, labSchedule.labName)
                        shift.contains("noche", ignoreCase = true) ->
                            setCheckBoxColor(eveningId, color, labSchedule.labName)
                    }
                }
            }
        }
    }

    private fun resetCheckBox(checkBoxId: Int) {
        val checkBox = findViewById<CheckBox>(checkBoxId)
        checkBox.isChecked = false
        checkBox.tag = null
    }

    private fun setCheckBoxColor(checkBoxId: Int, color: String, labName: String) {
        val checkBox = findViewById<CheckBox>(checkBoxId)
        checkBox.isChecked = true
        checkBox.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
        checkBox.tag = labName
    }

    private fun restoreAllColors(labSchedules: List<LabSchedule>) {
        labSchedules.forEachIndexed { labIndex, labSchedule ->
            val color = colors.getOrElse(labIndex % colors.size) { "#000000" }

            labSchedule.days.forEach { (dayName, shifts) ->
                val dayKey = when (dayName.lowercase()) {
                    "lunes" -> "Lunes"
                    "martes" -> "Martes"
                    "miercoles", "miércoles" -> "Miércoles"
                    "jueves" -> "Jueves"
                    "viernes" -> "Viernes"
                    "sabado", "sábado" -> "Sábado"
                    "domingo" -> "Domingo"
                    else -> dayName
                }

                val (morningId, afternoonId, eveningId) = daysMap[dayKey] ?: return@forEach

                shifts.forEach { shift ->
                    when {
                        shift.contains("mañana", ignoreCase = true) ->
                            setCheckBoxColor(morningId, color, labSchedule.labName)
                        shift.contains("tarde", ignoreCase = true) ->
                            setCheckBoxColor(afternoonId, color, labSchedule.labName)
                        shift.contains("noche", ignoreCase = true) ->
                            setCheckBoxColor(eveningId, color, labSchedule.labName)
                    }
                }
            }
        }
    }

    fun backViewAssignedSchedule(view: android.view.View) {
        finish()
    }

    //Methods to highlight labs only labs assigned to the user, no all labs
    private fun displayAssignedLabsOnly(labSchedules: List<LabSchedule>) {
        val container = findViewById<LinearLayout>(R.id.containerCheckBoxes)
        container.removeAllViews()

        labSchedules.forEachIndexed { index, labSchedule ->
            val color = colors.getOrElse(index % colors.size) { "#000000" }
            val checkBox = CheckBox(this).apply {
                text = labSchedule.labName
                isChecked = true
                isClickable = false
                textAlignment = LinearLayout.TEXT_ALIGNMENT_TEXT_START
                setTextAppearance(this@viewAssignedSchedule, R.style.formMessageStyle)
                buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    highlightLabSchedule(labSchedule.labName, color)
                }
            }
            container.addView(checkBox)
        }
    }
    private fun highlightLabSchedule(labName: String, color: String) {
        daysMap.values.forEach { (morningId, afternoonId, eveningId) ->
            resetCheckBox(morningId)
            resetCheckBox(afternoonId)
            resetCheckBox(eveningId)
        }

        daysMap.values.forEach { (morningId, afternoonId, eveningId) ->
            val morningCheck = findViewById<CheckBox>(morningId)
            val afternoonCheck = findViewById<CheckBox>(afternoonId)
            val eveningCheck = findViewById<CheckBox>(eveningId)

            if (morningCheck.tag == labName) {
                morningCheck.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            }
            if (afternoonCheck.tag == labName) {
                afternoonCheck.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            }
            if (eveningCheck.tag == labName) {
                eveningCheck.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            }
        }
    }




}