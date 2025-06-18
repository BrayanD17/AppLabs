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
    val nameForm: String = "",
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
    val uidForm : String = "",
    val idFormStudent:String = "",
    val carnet: String = "",
    val estado: String = "",
    val carrera : String = "",
    val numeroSemestreOperador : String = "",
)

data class FilterData(
    val degree: String? = null,
    val semester: String? = null,
    val name: String? = null,
    val cardStudent: String? = null,
    val status: String? = null,
)

data class FilterDataMisconduct(
    val nombre: String? = null,
    val carnet: String? = null,
    val semestres: String? = null,
    val laboratory: String? = null,
)

data class dataUpdateStatus(
    val newStatusApplication: Int,
    val newComment: String = "",
    val userId: String="",
    val subject: String = "",
    val message: String = ""
)

data class getMessage(
    val subject: String = "",
    val infomessage: String = "" ,
    val timestamp: String = "",
    val status: Int
)

data class formOperatorActive(
    val operatorIdForm: String = "",
    val nameActiveForm: String = "",
    val semesterActive: String = ""
)

data class FormListStudent(
    val FormIdStudent: String = "",
    val FormId: String = "",
    val Semester: String = "",
    val FormName: String = "",
    val DateEnd : String = "",
    val DateStart : String = "",
    val IsEdit:Boolean
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

data class UserInformation(
    val nameUser: String = "",
    val rolUser: String = ""
)

data class ReportMisconducStudent(
    val idOperator : String = "",
    val laboratory: String = "",
    val student : String = "",
    val semester: String = "",
    val comment: String = ""
)

data class MisconductStudent(
    val student : String = "",
    val email : String = "",
    val semester: String = "",
    val laboratory: String = "",
    val cardStudent: String = "",
    val id: String = "",  // uid del documento
)

data class ReportVisitStudent(
    val idOperator : String = "",
    val idstudent : String = "",
    val laboratory: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
)

data class ReportVisit(
    val student : String = "",
    val cardStudent: String = "",
    val laboratory: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
)

data class filterDataVisit(
    val name: String? = null,
    val cardStudent: String? = null,
    val laboratory: String? = null,
    val date: String? = null,
)

data class AssignedScheduleData(
    val name: String,
    val laboratory: String,
    val shift: String,
    val day: String,
    val operator: String,
    val scheduleMatrix: Map<String, List<String>>
)

data class LabSchedule(
    val labName: String,
    val days: Map<String, List<String>>
)

data class historySemesterOperator(
    val semester: String,
    val year: String,
    val date: String,
    val userId: List<String> = emptyList()
)

data class OperatorItem(
    val userId: String,
    val data: DataClass
)

data class FilterShowAllOperator(
    val carnetOperator: String? = null,
    val emailOperator: String? = null,
    val phoneOperator: String? = null
)


