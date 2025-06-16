package com.labs.applabs.operator

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.ScheduleItem
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

    private fun getScheduleAssigned(schedule: List<ScheduleItem>) {
        schedule.forEach { item ->
            val (morningId, afternoonId, eveningId) = daysMap[item.day] ?: return@forEach

            val morningCheckBox = findViewById<CheckBox>(morningId)
            val afternoonCheckBox = findViewById<CheckBox>(afternoonId)
            val eveningCheckBox = findViewById<CheckBox>(eveningId)

            morningCheckBox.isChecked = item.shift.contains("7am a 12pm")
            afternoonCheckBox.isChecked = item.shift.contains("12pm a 5pm")
            eveningCheckBox.isChecked = item.shift.contains("5 a 10pm")
        }
    }

    fun backViewAssignedSchedule(view: android.view.View) {
        finish()
    }
}