package com.labs.applabs.firebase

data class DataClass(
    val studentInfo: StudentInfo
)

data class StudentInfo(
    val applicationOperatorTitle: String = "",
    val typeForm: String = "",
    val studentName: String = "",
    val surNames: String = "",
    val studentId: String = "",              // cédula
    val studentCard: String = "",            // carnet
    val studentEmail: String = "",
    val studentPhone: String = "",
    val bankAccount: String = "",
    val accountClient: String = "",
    val studentLastDigitCard: String = "",
    val studentAverage: String = "",         // promedio
    val studentShifts: String = "",          // turnos
    val studentCareer: String = "",
    val studentSemester: String = "",
    val namePsycologist: String = "",        // nombre del psicólogo
    val scheduleAvailability: List<String> = emptyList()  // Suponiendo lista de horarios
)
