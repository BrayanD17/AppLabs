package com.labs.applabs.firebase
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.labs.applabs.models.FormOperador
import com.labs.applabs.models.Usuario
import com.labs.applabs.student.FormStudentData
import kotlinx.coroutines.tasks.await

class Provider {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    // Obtener UID del usuario autenticado
    private fun getAuthenticatedUserId(): String {
        val currentUser = auth.currentUser
        return currentUser?.uid ?: throw IllegalStateException("No hay usuario autenticado")
    }

    // Registrar usuario en FirebaseAuth + Firestore y enviar correo de verificación
    fun registrarUsuario(
        context: Context,
        correo: String,
        password: String,
        usuario: Usuario,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val usuarioFinal = usuario.copy(uid = uid ?: "")

                    db.collection("users").document(uid!!)
                        .set(usuarioFinal)
                        .addOnSuccessListener {
                            // Enviar correo de verificación
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnSuccessListener {
                                    Toast.makeText(context, "Registro exitoso. Verifica tu correo.", Toast.LENGTH_LONG).show()
                                    onSuccess()
                                }
                                ?.addOnFailureListener {
                                    onError("Usuario creado pero no se pudo enviar verificación: ${it.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            onError("Error al guardar: ${e.message}")
                        }
                } else {
                    onError("Error de registro: ${task.exception?.message}")
                }
            }
    }

    // Obtener rol del usuario desde Firestore (por UID)
    fun verificarRol(uid: String, onResult: (Int) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val rol = doc.getLong("rol")?.toInt() ?: 3 // Por defecto: estudiante
                onResult(rol)
            }
            .addOnFailureListener {
                onResult(3)
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

    suspend fun saveStudentData(studentData: FormStudentData): Boolean {
        return try {
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

    suspend fun uploadPdfToFirebase(pdfUri: Uri): String? {
        val fileName = "${System.currentTimeMillis()}.pdf"
        val pdfRef = storageRef.child(fileName)

        try {
            pdfRef.putFile(pdfUri).await()
            val downloadUrl = pdfRef.downloadUrl.await()
            return downloadUrl.toString()
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

    fun createFormularioOperador(formulario: FormOperador, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("formOperator")
            .add(formulario)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

}
