package com.labs.applabs.firebase
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Provider {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserInfo(userId: String): DataClass? {
        return try {
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

    suspend fun getFormStudent(userId: String): DataClass?  {
        return try {
            val doc = db.collection("formStudent").document(userId).get().await()
            if(doc.exists()){
                val studentInfo = StudentInfo(
                    idFormOperator= doc.getString("idFormOperator") ?: "",
                    /*studentCareer
                    comment
                    studentLastDigitCard
                    studentId
                    idFormOperator
                    idUser
                    namePsycologist
                    scheduleAvailability
                    studentSemester
                    statusApplication
                    urlApplication
                    studentAverage*/
                )
                DataClass(studentInfo = studentInfo)
            }else null
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos para $userId: ${e.message}")
            null
        }
    }


    //suspend fun getFormOperator(formId: String): Map<String, Any>? {}




    //suspend fun updateStatus(formId: String): Map<String, Any>? {}


}