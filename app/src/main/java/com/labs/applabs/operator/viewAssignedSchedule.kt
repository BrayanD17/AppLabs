package com.labs.applabs.operator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R

class viewAssignedSchedule : AppCompatActivity() {

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
    }

    fun backViewAssignedSchedule(view: android.view.View) {
        finish()
    }
}