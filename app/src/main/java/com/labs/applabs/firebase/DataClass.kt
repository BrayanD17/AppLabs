package com.labs.applabs.firebase

data class DataClass(
    val studentInfo: StudentInfo = StudentInfo(),
    val formOperator: FormOperator = FormOperator()
)

data class StudentInfo(
    //Data users
    val studentName: String = "",
    val surNames: String = "",
    val studentCard: String = "",            // carnet
    val studentEmail: String = "",
    val studentPhone: String = "",
    val bankAccount: String = "",
    //Data formStudent
    val studentCareer: String = "",
    val comment: String = "",
    val studentLastDigitCard: String = "",
    val studentId: String = "",              // cédula
    val idFormOperator: String = "",
    val idUser: String = "",
    val namePsycologist: String = "",
    val scheduleAvailability: List<String> = emptyList(),
    val studentSemester: String = "",
    val studentShifts: String="",
    val statusApplication: String = "",
    val urlApplication: String = "",
    val studentAverage: String = "",         // promedio
)

data class FormOperator(
    val applicationOperatorTitle: String = "",
    val typeForm: String = "",
    val year: String = "",
)
