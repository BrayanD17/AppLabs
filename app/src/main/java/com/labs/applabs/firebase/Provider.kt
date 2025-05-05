package com.labs.applabs.firebase
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.student.FormStudentData
import kotlinx.coroutines.tasks.await

class Provider {
    private val db = FirebaseFirestore.getInstance()

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
    fun saveStudentData(studentData: FormStudentData) {
        val user = getAuthenticatedUserId()
        try {
            // Convertimos el objeto completo a un mapa compatible con Firestore
            val dataMap = hashMapOf<String, Any>(
                "idStudent" to user,
                "idCard" to studentData.idCard,
                "weightedAverage" to studentData.weightedAverage,
                "degree" to studentData.degree,
                "phoneNumber" to studentData.phoneNumber,
                "IdSchoolNumber" to studentData.IdSchoolNumber,
                "shift" to studentData.shift,
                "semester" to studentData.semester,
                "psychology" to studentData.psychology,
                "ticketUrl" to studentData.ticketUrl,
                "schedule" to studentData.schedule.map { daySchedule ->
                    hashMapOf(
                        "day" to daySchedule.day,
                        "shifts" to daySchedule.shifts  // Lista de Strings directamente
                    )
                }
            )


            // Guardamos en Firestore
            db.collection("Forms")
                .add(dataMap)
                .addOnSuccessListener {
                    val context = this@Provider as? Context ?: return@addOnSuccessListener
                    Toast.makeText(this@Provider, "¡Guardado!", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener {
                    val context = this@Provider as? Context ?: return@addOnFailureListener
                    Toast.makeText(this@Provider, "Error", Toast.LENGTH_LONG).show()}

        } catch (e: Exception) {
            Log.e("Firebase", "Error al guardar", e)
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
    fun saveStudentData(studentData: FormStudentData) {
        val user = getAuthenticatedUserId()
        try {
            // 1. Creación del mapa de datos más eficiente
            val dataMap = hashMapOf<String, Any>().apply {
                put("idStudent", user)
                put("idCard", studentData.idCard)
                put("weightedAverage", studentData.weightedAverage)
                put("degree", studentData.degree)
                put("phoneNumber", studentData.phoneNumber)
                put("IdSchoolNumber", studentData.IdSchoolNumber)
                put("shift", studentData.shift)
                put("semester", studentData.semester)
                put("psychology", studentData.psychology)
                put("ticketUrl", studentData.ticketUrl)
                put("schedule", studentData.schedule.map { daySchedule ->
                    hashMapOf(
                        "day" to daySchedule.day,
                        "shifts" to daySchedule.shifts
                    )
                })
            }

            // 2. Guardado con manejo de contexto más seguro
            val context = (this as? Context) ?: run {
                Log.e("Provider", "Contexto no disponible")
                return
            }

            db.collection("Forms")
                .add(dataMap)
                .addOnSuccessListener {
                    Toast.makeText(context, "¡Guardado!", Toast.LENGTH_SHORT).show()
                    FormStudentData.clearAll()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("Firestore", "Error al guardar", e)
                }

        } catch (e: Exception) {
            Log.e("Firebase", "Error al guardar", e)
            (this as? Context)?.let {
                Toast.makeText(it, "Error inesperado", Toast.LENGTH_LONG).show()
            }
        }
    }

    //
    fun SolicitudURL(context: Context) {

        db.collection("formOperator")
            .whereEqualTo("activityStatus", 1)
            .limit(1)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (!queryDocumentSnapshots.isEmpty) {
                    val document = queryDocumentSnapshots.documents[0]
                    val originalUrl = document.getString("urlApplicationForm")

                    if (!originalUrl.isNullOrEmpty()) {
                        // Convertir a enlace de descarga directa si es de Google Drive
                        val directDownloadUrl = if (originalUrl.contains("drive.google.com/open?id=")) {
                            val fileId = originalUrl.substringAfter("id=")
                            "https://drive.google.com/uc?export=download&id=$fileId"
                        } else {
                            originalUrl // si ya es descarga directa
                        }

                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(directDownloadUrl))
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "El documento no tiene URL", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "No hay documentos habilitados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al consultar Firestore", Toast.LENGTH_SHORT).show()
            }
    suspend fun getFormStudent(formId: String): DataClass?  {
        return try {
            val doc = db.collection("formStudent").document(formId).get().await()
            if(doc.exists()){
                val scheduleAvailability = (doc.get("scheduleAvailability") as? Map<*, *>)?.map {
                    "${it.key}: ${it.value}"
                } ?: emptyList()
                val studentInfo = StudentInfo(
                    studentCareer= doc.getString("career") ?: "",
                    comment= doc.getString("comment") ?: "",
                    studentLastDigitCard= doc.get("digitsCard")?.toString() ?: "",
                    studentId= doc.get("idCard")?.toString() ?: "",
                    idFormOperator= doc.getString("idFormOperator") ?: "",
                    idUser= doc.getString("idUser") ?: "",
                    namePsycologist= doc.getString("psychology") ?: "",
                    scheduleAvailability= scheduleAvailability,
                    studentSemester= doc.get("semesterNumber")?.toString() ?: "",
                    studentShifts= doc.get("shifts")?.toString() ?: "",
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


    suspend fun getFormOperator(formId: String?): DataClass? {
        return try {
            if (formId == null) return null
            val doc = db.collection("formOperator").document(formId).get().await()
            if(doc.exists()){
                val formOperator = FormOperator(
                    applicationOperatorTitle = doc.getString("nameForm") ?: "",
                    typeForm = doc.getString("semester") ?: "",
                    year = doc.get("year")?.toString() ?: "",
                )
                DataClass(formOperator = formOperator)
            }else null
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos para $formId: ${e.message}")
            null
        }

    }




    //suspend fun updateStatus(formId: String): Map<String, Any>? {}



}