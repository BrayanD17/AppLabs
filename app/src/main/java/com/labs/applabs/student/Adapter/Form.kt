package com.labs.applabs.student.Adapter

data class Form(val id: String,
                val idStudent: String,
                val idCard : Int,
                val weightedAverage: Float,
                val degree : String,
                val idSchoolNumber : Int,
                val shifts : Int,
                val scheduleHours: List<String> = emptyList(),
                val semestersOperator: Int = 0,
                val psichologyName: String = "",
                val ticketURL: String = ""
)
