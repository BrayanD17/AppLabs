package com.labs.applabs.firebase
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.labs.applabs.student.FormStudentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Provider {
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    // Funcion paraa poder obtener el id de la persona autenticada
    private fun getAuthenticatedUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: throw IllegalStateException("No hay usuario autenticado")
    }

    suspend fun getCareerNames(): List<String> {
        return try {
            val snapshot = db.collection("dataDefault").document("careers").get().await()
            snapshot.get("career") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("Firebase", "Error al cargar escuelas", e)
            emptyList()
        }
    }

    suspend fun getUserInfo(userId: String?): DataClass? {
        return try {
            if (userId == null) return null
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val studentInfo = StudentInfo(
                    studentName = doc.getString("name") ?: "",
                    surNames= doc.getString("surnames") ?: "",
                    studentCard = doc.get("studentCard")?.toString() ?: "",
                    studentEmail = doc.getString("email") ?: "",
                    studentPhone = doc.get("phone")?.toString() ?: "",
                    bankAccount = doc.getString("bankAccount") ?: "",
                )
                DataClass(studentInfo = studentInfo)
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos para $userId: ${e.message}")
            null
        }
    }

    //
    suspend fun saveStudentData(studentData: FormStudentData): Boolean {
        return try {
            // val user = getAuthenticatedUserId()
            val user = "gfTos90dNJeX8kkffqIo"

            val dataMap = hashMapOf<String, Any>().apply {
                put("idStudent", user)
                put("idCard", studentData.idCard.toInt())
                put("weightedAverage", studentData.weightedAverage.toInt())
                put("degree", studentData.degree)
                put("digitsCard", studentData.digitsCard.toInt())
                put("shift", studentData.shift.toInt())
                put("semester", studentData.semester.toInt())
                put("psychology", studentData.psychology)
                put("urlApplicationForm", studentData.ticketUrl)
                put("comment", studentData.comment)
                put("idFormOperator", studentData.idFormOperator)
                put("statusApplicationForm", 0 )
                put("scheduleAvailability", studentData.schedule.map { daySchedule ->
                    hashMapOf(
                        "day" to daySchedule.day,
                        "shifts" to daySchedule.shifts
                    )
                })
            }

            db.collection("formStudent")
            .add(dataMap)
            .await()

            FormStudentData.clearAll()
            true

        } catch (e: Exception) {
            Log.e("Firebase", "Error al guardar: ${e.message}", e)
            false
        }
    }


    suspend fun getFormStudent(formId: String): DataClass?  {
        return try {
            val doc = db.collection("formStudent").document(formId).get().await()
            if(doc.exists()){
                val scheduleRaw = doc.get("scheduleAvailability") as? List<Map<String, Any>> ?: emptyList()
                val scheduleAvailability = scheduleRaw.map {
                    val day = it["day"] as? String ?: ""
                    val shifts = it["shifts"] as? List<String> ?: emptyList()
                    ScheduleItem(day, shifts)
                }
                val studentInfo = StudentInfo(
                    studentCareer= doc.getString("degree") ?: "",
                    comment= doc.getString("comment") ?: "",
                    studentLastDigitCard= doc.get("digitsCard")?.toString() ?: "",
                    studentId= doc.get("idCard")?.toString() ?: "",
                    idFormOperator= doc.getString("idFormOperator") ?: "",
                    idUser= doc.getString("idStudent") ?: "",
                    namePsycologist= doc.getString("psychology") ?: "",
                    scheduleAvailability= scheduleAvailability,
                    studentSemester= doc.get("semester")?.toString() ?: "",
                    studentShifts= doc.get("shift")?.toString() ?: "",
                    statusApplication= doc.get("statusApplicationForm")?.toString() ?: "",
                    urlApplication= doc.getString("urlApplicationForm") ?: "",
                    studentAverage= doc.get("weightedAverage")?.toString() ?: "",
                )
                DataClass(studentInfo = studentInfo)
            }else null
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos para $formId: ${e.message}")
            null
        }
    }

    suspend fun updateStudentData(formId: String, studentData: editDataStudentForm): Boolean {
        return try {
            val userId = "MWEPEbXrAFTpeY5V57znaeCbuh83" // Aquí deberías usar FirebaseAuth.getInstance().currentUser?.uid

            val docRef = db.collection("formStudent").document(formId)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                Log.e("Firebase", "Formulario no encontrado")
                return false
            }

            val storedUserId = snapshot.getString("idStudent")
            if (storedUserId != userId) {
                Log.e("Firebase", "No autorizado para actualizar este formulario")
                return false
            }

            val dataMap = hashMapOf<String, Any>().apply {
                put("idCard", studentData.dataCardId.toInt())
                put("weightedAverage", studentData.dataAverage.toInt())
                put("degree", studentData.dataDegree)
                put("digitsCard", studentData.dataLastDigits.toInt())
                put("shift", studentData.dataShifts.toInt())
                put("semester", studentData.dataSemesterOperator.toInt())
                put("psychology", studentData.dataNamePsychology)
                put("urlApplicationForm", studentData.dataUploadPdf)
                put("scheduleAvailability", studentData.datatableScheduleAvailability.map { daySchedule ->
                    hashMapOf(
                        "day" to daySchedule.day,
                        "shifts" to daySchedule.shifts
                    )
                })
            }

            docRef.set(dataMap, SetOptions.merge()).await()
            true

        } catch (e: Exception) {
            Log.e("Firebase", "Error al actualizar: ${e.message}", e)
            false
        }
    }

    suspend fun getFormOperator(idFormOperator: String?): DataClass? {
        return try {
            if (idFormOperator == null) return null
            val doc = db.collection("formOperator").document(idFormOperator).get().await()
            if(doc.exists()){
                val formOperator = FormOperator(
                    applicationOperatorTitle = doc.getString("nameForm") ?: "",
                    typeForm = doc.getString("semester") ?: "",
                    year = doc.get("year")?.toString() ?: "",
                )
                DataClass(formOperator = formOperator)
            }else null
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos para $idFormOperator: ${e.message}")
            null
        }

    }

    //Actualizar el estado y comentario
    suspend fun updateFormStatusAndComment(formId: String, updateData: dataUpdateStatus): Boolean {
        return try {
            val formRef = db.collection("formStudent").document(formId)
            formRef.update(
                "statusApplicationForm", updateData.newStatusApplication,
                "comment", updateData.newComment
            ).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseError", "Error al actualizar los datos", e)
            false
        }
    }

    // Esta función será suspendida para poder usarse con coroutines
    suspend fun uploadPdfToFirebase(pdfUri: Uri): String? {

        val fileName = "${System.currentTimeMillis()}.pdf"
        val pdfRef = storageRef.child(fileName)

        try {
            // Subir el archivo de manera asíncrona y obtener el URL de descarga
            pdfRef.putFile(pdfUri).await()
            val downloadUrl = pdfRef.downloadUrl.await()
            return downloadUrl.toString() // Devuelves el URL del archivo subido
        } catch (e: Exception) {
            throw Exception("Error al subir el archivo: ${e.message}")
        }
    }

    suspend fun getFormOperatorData(): FormOperatorData? {
        return try {
            val doc = db.collection("formOperator")
                .whereEqualTo("activityStatus", 1)
                .get()
                .await()

            val document = doc.documents.firstOrNull()
            if (document != null) {
                FormOperatorData(
                    urlApplicationForm = document.getString("urlApplicationForm"),
                    iud = document.getString("iud"),
                    nameForm = document.getString("nameForm"),
                    semester = document.getString("semester"),
                    year = document.getString("year")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    suspend fun getSolicitudes(): List<Solicitud> {
        return try {
            // val activeFormId = getFormOperatorData()?.iud ?: return emptyList()

            val snapshot = db.collection("formStudent")
                .whereEqualTo("idFormOperator ", "0OyPvJVUXD7aamtEHR1a")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val idUser = doc.getString("idStudent") ?: return@mapNotNull null

                val userDoc = db.collection("users").document(idUser).get().await()
                if (!userDoc.exists()) return@mapNotNull null

                val nombre = userDoc.getString("name") ?: ""
                val correo = userDoc.getString("email") ?: ""

                Solicitud(nombre = nombre, correo = correo)
            }

        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener solicitudes: ${e.message}")
            emptyList()
        }
    }


    suspend fun deletePdfFromStorage(url: String) {
        try {
            val storageRef = Firebase.storage.getReferenceFromUrl(url)
            storageRef.delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error al eliminar archivo", e)
        }
    }

    suspend fun uploadPdfToStorage(uri: Uri, fileName: String): String {
        val storageRef = Firebase.storage.reference.child(fileName)
        val uploadTask = storageRef.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }


    //Obtener los formularios que ha enviado el estudiante
    suspend fun getInfoStudentForm(id:String): List<FormListStudent> {
        return try {
            val snapshot = db.collection("formStudent")
                .whereEqualTo("idStudent", id)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val formStudentDocId = doc.id
                val formId = doc.getString("idFormOperator") ?: return@mapNotNull null

                val listIdInfo = db.collection("formOperator").document(formId).get().await()
                if (!listIdInfo.exists()) return@mapNotNull null

                val semester = listIdInfo.getString("semester") ?: ""
                val year = listIdInfo.get("year") ?.toString()?: ""
                val startDateObj = listIdInfo.getTimestamp("startDate")?.toDate()
                val closingDateObj = listIdInfo.getTimestamp("closingDate")?.toDate()

                val currentDate = Date()
                val isEditable = startDateObj != null && closingDateObj != null &&
                        currentDate.after(startDateObj) && currentDate.before(closingDateObj)

                val closingDateFormatted = closingDateObj?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: ""

                val startDateFormatted = startDateObj?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: ""


                FormListStudent(
                    FormIdStudent = formStudentDocId,
                    FormId = formId,
                    Semester = "$semester $year",
                    FormName = listIdInfo.getString("nameForm") ?: "",
                    DateEnd = closingDateFormatted,
                    DateStart = startDateFormatted,
                    IsEdit = isEditable
                )
            }

        }catch (e: Exception){
            Log.e("FirestoreProvider", "Error al obtener getInfoStudentForm: ${e.message}")
            emptyList()
        }

    }

    fun deleteFormStudent(docId: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("formStudent")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Documento eliminado correctamente")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al eliminar el documento", e)
                onResult(false)
            }
    }



}


