package com.labs.applabs.firebase

data class DataClass(
    val studentInfo: StudentInfo = StudentInfo(),
    val formOperator: FormOperator = FormOperator()

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

data class FormOperatorData(
    val urlApplicationForm: String?,
    val iud: String?,
    val nameForm : String?,
    val semester: String?,
    val year: String?

)

data class Solicitud(
    val nombre: String = "",
    val correo: String = "",
    val uid : String = "",
)

data class dataUpdateStatus(
    val newStatusApplication: Int,
    val newComment: String = "",
)

data class FormListStudent(
    val Semester: String = "",
    val FormName: String = "",
    val DateEnd : String = "",
)