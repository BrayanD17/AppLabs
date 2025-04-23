package com.labs.applabs.firebase
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class provider {
    private val db = FirebaseFirestore.getInstance()

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



}