package com.labs.applabs.firebase
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.labs.applabs.student.FormStudentData
import kotlinx.coroutines.tasks.await
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

    /*Using coroutines to fetch data asynchronously*/
    /*Está función es solo de ejemplo, no es funcional dentro del proyecto (solo era una prueba)
    * Forma de instanciar provider para usarlo en el frontend ( val provider: provider = provider() )*/
    suspend fun getFormularioInfoById(id: String): Map<String, Any>? {
        return try {
            val snapshot = db.collection("prueba").document(id).get().await()
            if (snapshot.exists()) {
                snapshot.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos: ${e.message}")
            null
        }
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
                put("idFormOperator ", studentData.idFormOperator)
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
                    idFormOperator= doc.getString("idFormOperator ") ?: "",
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

    //Update status and comment
    suspend fun updateFormStatusAndComment(formId: String, updateData: dataUpdateStatus): Boolean {
        return try {
            val formRef = db.collection("formStudent").document(formId)
            formRef.update(
                "statusApplicationForm", updateData.newStatusApplication,
                "comment", updateData.newComment
            ).await()
            generateNotificationMessage(updateData)
            true
        } catch (e: Exception) {
            Log.e("FirebaseError", "Error al actualizar los datos", e)
            false
        }
    }

    suspend fun generateNotificationMessage(updateData: dataUpdateStatus) {
        val messagesCollection = db.collection("message")
        val userMessagesRef = messagesCollection.document(updateData.userId)
        val notificationsCollection = userMessagesRef.collection("notifications")

        val newNotification = hashMapOf(
            "subject" to "Proceso de solicitud",
            "message" to updateData.message,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to 0
        )

        notificationsCollection.add(newNotification).await()
    }

    suspend fun getUserMessages(userId: String): List<getMessage> {
        val notificationsRef = db.collection("message")
            .document(userId)
            .collection("notifications")

        val querySnapshot = notificationsRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val dateFormat = SimpleDateFormat("d/M/yyyy h:mm", Locale.getDefault())

        return querySnapshot.documents.mapNotNull { doc ->
            try {
                val subject = doc.getString("subject") ?: ""
                val message = doc.getString("message") ?: ""
                val date = doc.getTimestamp("timestamp")?.toDate()
                val timestamp  = date?.let { dateFormat.format(it) } ?: ""
                val status = doc.getLong("status")?.toInt() ?: 0

                getMessage(subject, message, timestamp, status)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun markMessagesAsSeen(userId: String) {
        val notificationsRef = db.collection("message")
            .document(userId)
            .collection("notifications")

        //Get all unread messages
        val querySnapshot = notificationsRef
            .whereEqualTo("status", 0)
            .get()
            .await()

        //Update each unread message in a batch
        val batch = db.batch()
        querySnapshot.documents.forEach { doc ->
            batch.update(doc.reference, "status", 1)
        }

        if (!querySnapshot.isEmpty) {
            batch.commit().await()
        }
    }


    // Esta función será suspendida para poder usarse con coroutines
    suspend fun uploadPdfToFirebase(pdfUri: Uri): String {

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

    suspend fun getFormOperatorUrl() : String? {
        try {
            val doc = db.collection("formOperator").whereEqualTo("activityStatus", 1).get().await()
            return doc.documents.firstOrNull()?.getString("urlApplicationForm")
        } catch (e: Exception){
            throw Exception("Error ${e.message}")
        }
    }

    suspend fun saveFcmToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            db.collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await()

            Log.d("FCM", "Token guardado en campo")
        } catch (e: Exception) {
            Log.e("FCM", "Error completo:", e)
            throw e
        }
    }

}


