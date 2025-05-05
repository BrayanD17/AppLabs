package com.labs.applabs.student

object FormStudentData {
    var uid: String = ""
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

    fun clearAll() {
        uid = ""
        idCard = ""
        weightedAverage = 0f
        degree = ""
        phoneNumber = ""
        IdSchoolNumber = 0
        shift = 0
        schedule = emptyList()
        semester = 0
        psychology = ""
        ticketUrl = ""
    }

}
data class DaySchedule(
    val day: String,
    val shifts: List<String> = emptyList()
)
