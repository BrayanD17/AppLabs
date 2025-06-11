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
            "Lunes" to Triple(R.id.mondayMorning, R.id.mondayAfternoon, R.id.mondayEvening),
            "Martes" to Triple(R.id.tuesdayMorning, R.id.tuesdayAfternoon, R.id.tuesdayEvening),
            "Miércoles" to Triple(R.id.wednesdayMorning, R.id.wednesdayAfternoon, R.id.wednesdayEvening),
            "Jueves" to Triple(R.id.thursdayMorning, R.id.thursdayAfternoon, R.id.thursdayEvening),
            "Viernes" to Triple(R.id.fridayMorning, R.id.fridayAfternoon, R.id.fridayEvening),
            "Sábado" to Triple(R.id.saturdayMorning, R.id.saturdayAfternoon, R.id.saturdayEvening),
            "Domingo" to Triple(R.id.sundayMorning, R.id.sundayAfternoon, R.id.sundayEvening)
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