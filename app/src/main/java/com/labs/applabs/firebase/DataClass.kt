package com.labs.applabs.firebase

data class DataClass(
    val studentInfo: StudentInfo = StudentInfo(),
    val formOperator: FormOperator = FormOperator(),
)

data class StudentInfo(
    //Data users
    val studentName: String = "",
    val surNames: String = "",
    val studentCard: String = "",
    val studentEmail: String = "",
    val studentPhone: String = "",
    val bankAccount: String = "",
    //Data formStudent
    val studentCareer: String = "",
    val comment: String = "",
    val studentLastDigitCard: String = "",
    val studentId: String = "",
    val idFormOperator: String = "",
    val idUser: String = "",
    val namePsycologist: String = "",
    val scheduleAvailability: List<ScheduleItem> = emptyList(),
    val studentSemester: String = "",
    val studentShifts: String="",
    val statusApplication: String = "",
    val urlApplication: String = "",
    val studentAverage: String = "",
)

data class ScheduleItem(
    val day: String = "",
    val shift: List<String> = emptyList()
)


data class FormOperator(
    val applicationOperatorTitle: String = "",
    val typeForm: String = "",
    val year: String = "",
)

data class dataUpdateStatus(
    val newStatusApplication: Int,
    val newComment: String = "",
    val userId: String="",
    val message: String = ""
)

data class getMessage(
    val subject: String = "",
    val infomessage: String = "" ,
    val timestamp: String = "",
    val status: Int
)
