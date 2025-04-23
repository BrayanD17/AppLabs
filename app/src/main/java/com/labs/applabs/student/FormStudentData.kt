package com.labs.applabs.student

object FormStudentData {
    var idCard: String = ""
    var weightedAverage: Float = 0f
    var degree: String = ""
    var phoneNumber: String = ""
    var IdSchoolNumber : Int = 0
    var shift : Int = 0
    var schedule : List<DaySchedule> = emptyList()
    var semester : Int = 0
    var psychology : String = ""
    var ticketUrl : String = ""

}

data class DaySchedule(
    val day: String,
    val shifts: List<String> = emptyList()
)