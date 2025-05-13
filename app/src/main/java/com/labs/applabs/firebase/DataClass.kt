package com.labs.applabs.firebase

data class DataClass(
    val studentInfo: StudentInfo = StudentInfo(),
    val formOperator: FormOperator = FormOperator(),
    val editDataStudentForm: editDataStudentForm = editDataStudentForm(),

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
    val FormId: String = "",
    val StudentName: String = "",
    val Semester: String = "",
    val FormName: String = "",
    val DateEnd : String = "",
    val DateStart : String = "",
)

data class editDataStudentForm(
    val dataCardId: String = "",
    val dataAverage: String = "",
    val dataDegree: String = "",
    val dataLastDigits: String = "",
    val dataShifts: String = "",
    val dataSemesterOperator: String = "",
    val dataNamePsychology: String = "",
    val datatableScheduleAvailability: List<listSchedule> = emptyList(),
    val dataUploadPdf: String = ""
)

data class listSchedule(
    val day: String,
    val shifts: List<String> = emptyList()
)