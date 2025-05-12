package com.labs.applabs.student

object FormStudentData {
    var uid: String = ""
    var idCard: String = ""
    var weightedAverage: String = ""
    var degree: String = ""
    var digitsCard : String = ""
    var shift : String = ""
    var schedule : List<DaySchedule> = emptyList()
    var semester : String = ""
    var psychology : String = ""
    var ticketUrl : String = ""
    var comment : String = "Pendiente de Revision"
    var idFormOperator : String = "0OyPvJVUXD7aamtEHR1a"


    fun clearAll() {
        uid = ""
        idCard = ""
        weightedAverage = ""
        degree = ""
        digitsCard = ""
        shift = ""
        schedule = emptyList()
        semester = ""
        psychology = ""
        ticketUrl = ""
        comment = "Pendiente de Revision"
        idFormOperator  = "0OyPvJVUXD7aamtEHR1a"
    }

}
data class DaySchedule(
    val day: String,
    val shifts: List<String> = emptyList()
)
