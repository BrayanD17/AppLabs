package com.labs.applabs.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.models.Usuario
import com.labs.applabs.student.FormStudentData
import kotlinx.coroutines.tasks.await

class Provider {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    //Obtener UID del usuario autenticado
    private fun getAuthenticatedUserId(): String {
        val currentUser = auth.currentUser
        return currentUser?.uid ?: throw IllegalStateException("No hay usuario autenticado")
    }

    //Registrar usuario en Firebase Authentication y Firestore
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

                    db.collection("usuarios").document(uid!!)
                        .set(usuarioFinal)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError("Error al guardar: ${e.message}")
                        }
                } else {
                    onError("Error de registro: ${task.exception?.message}")
                }
            }
    }

    //Obtener rol del usuario desde Firestore (por UID)
    fun verificarRol(uid: String, onResult: (Int) -> Unit) {
        db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val rol = doc.getLong("rol")?.toInt() ?: 3 // Por defecto: estudiante
                onResult(rol)
            }
            .addOnFailureListener {
                onResult(3)
            }
    }

    //Guardar datos de formulario del estudiante
    fun saveStudentData(studentData: FormStudentData) {
        val user = getAuthenticatedUserId()
        try {
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
                        "shifts" to daySchedule.shifts
                    )
                }
            )

            db.collection("Forms")
                .add(dataMap)
                .addOnSuccessListener {
                    Log.d("Provider", "Formulario guardado correctamente")
                }
                .addOnFailureListener {
                    Log.e("Provider", "Error al guardar formulario: ${it.message}")
                }

        } catch (e: Exception) {
            Log.e("Provider", "Excepción al guardar datos", e)
        }
    }


    suspend fun getFormularioInfoById(id: String): Map<String, Any>? {
        return try {
            val snapshot = db.collection("prueba").document(id).get().await()
            if (snapshot.exists()) snapshot.data else null
        } catch (e: Exception) {
            Log.e("Provider", "Error al obtener datos: ${e.message}")
            null
        }
    }
}
