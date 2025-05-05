package com.labs.applabs.firebase
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class provider {
    private val db = FirebaseFirestore.getInstance()

    /*Using coroutines to fetch data asynchronously*/
    /*Está función es solo de ejemplo, no es funcional dentro del proyecto (solo era una prueba)
    * Forma de instanciar provider para usarlo en el frontend ( val provider: provider = provider() )*/

    /*suspend fun getUserInfo(userId: String): Map<String, Any>? {}

    suspend fun getFormOperator(formId: String): Map<String, Any>? {}

    suspend fun getFormStudent(userId: String): Map<String, Any>? {
        return try {
            val snapshot = db.collection("formStudent").document(userId).get().await()
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


    suspend fun updateStatus(formId: String): Map<String, Any>? {}

*/

}