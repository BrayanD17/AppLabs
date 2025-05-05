package com.labs.applabs.firebase
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Provider {
    private val db = FirebaseFirestore.getInstance()

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


    //suspend fun getFormOperator(formId: String): Map<String, Any>? {}




    //suspend fun updateStatus(formId: String): Map<String, Any>? {}


}