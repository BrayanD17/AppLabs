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
    }



}