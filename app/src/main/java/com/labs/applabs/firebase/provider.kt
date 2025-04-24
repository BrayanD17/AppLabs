package com.labs.applabs.firebase
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
                .addOnSuccessListener { Toast.makeText(this@Provider, "¡Guardado!", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { e -> Toast.makeText(this@Provider, "Error: ${e.message}", Toast.LENGTH_LONG).show()}

        } catch (e: Exception) {
            Log.e("Firebase", "Error al guardar", e)
        }
    }



}