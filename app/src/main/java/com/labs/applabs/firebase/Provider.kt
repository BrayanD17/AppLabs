package com.labs.applabs.firebase
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.labs.applabs.models.FormOperador
import com.labs.applabs.models.Usuario
import com.labs.applabs.administrator.operator.OperadorCompleto
import com.labs.applabs.student.FormStudentData
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar
import kotlin.math.log

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
        email: String,
        password: String,
        usuario: Usuario,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
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
                val rol = doc.getLong("userRole")?.toInt() ?: 2 // Por defecto: estudiante
                onResult(rol)
            }
            .addOnFailureListener {
                onResult(2)
            }
    }

    suspend fun getCareerNames(): List<String> {
        return try {
            val snapshot = db.collection("dataDefault").document("careers").get().await()
            snapshot.get("career") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("Firebase", "Error al cargar carreras", e)
            emptyList()
        }
    }

    suspend fun getFormStatusData() : List<String> {
        return try{
            val snapshot = db.collection("dataDefault").document("statusForm").get().await()
            snapshot.get("status") as? List<String> ?: emptyList()
        } catch (e : Exception){
            Log.e("Firebase", "Error al cargar estados", e)
            emptyList()
        }
    }

    suspend fun getLaboratoryName() : List<String> {
        return try{
            val laboratories = db.collection("dataDefault").document("laboratories").get().await()
            laboratories.get("laboratory") as? List<String> ?: emptyList()
        } catch (e : Exception){
            Log.e("Firebase", "Error al cargar laboratorios", e)
            emptyList()
        }
    }

    suspend fun getStudentName() : Map<String, String> {
        return try{
            val students = db.collection("users").whereEqualTo("userRole", 2).get().await()
            students.documents.mapNotNull { doc ->
                val name = doc.get("name")
                val surNames = doc.get("surnames")
                val fullname = "$name $surNames".trim()
                val id = doc.id
                if (name != null && surNames != null) {
                    fullname to id
                } else {
                    null
                }
            }.toMap()
        } catch (e : Exception){
            Log.e("Firebase", "Error al cargar estudiantes")
            emptyMap()
        }

    }


    suspend fun getAllOperadores(): List<OperadorCompleto> {
        val db = FirebaseFirestore.getInstance()
        val historial = db.collection("historialOperadores").get().await()
        val lista = mutableListOf<OperadorCompleto>()
        for (doc in historial.documents) {
            val userId = doc.getString("userId") ?: continue
            val formId = doc.getString("formId") ?: continue

            // Fetch usuario
            val userDoc = db.collection("users").document(userId).get().await()
            val carnet = userDoc.getString("studentCard") ?: ""
            val nombre = userDoc.getString("name") + " " + (userDoc.getString("surnames") ?: "")
            val correo = userDoc.getString("email") ?: ""

            // Fetch formStudent
            val formDoc = db.collection("formStudent").document(formId).get().await()
            val carrera = formDoc.getString("degree") ?: ""

            lista.add(OperadorCompleto(userId, carnet, nombre, carrera, correo))
        }
        return lista
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
            val user = getAuthenticatedUserId()

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

    suspend fun saveStudentMisconductos(reportMisconducStudent: ReportMisconducStudent): Boolean {
        return try{
            val user = getAuthenticatedUserId()
            val dataMapMisconductReport = hashMapOf<String, Any>().apply {
                put("idOperador", user)
                put("laboratory", reportMisconducStudent.laboratory)
                put("student", reportMisconducStudent.student)
                put("semester", reportMisconducStudent.semester)
                put("comment", reportMisconducStudent.comment)
            }
            db.collection("misconducReportStudent")
                .add(dataMapMisconductReport)
                .await()
            return true
        } catch (e : Exception){
            Log.e("Firebase", "Error al guardar el reporte de mala conducta.(${e.message})")
            return false
        }
    }

    //Obtener los datos del estudiente de formStudent por id de usuario
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
                    idFormOperator= doc.getString("idFormOperator") ?: "",
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

    suspend fun updateStudentData(formId: String, studentData: editDataStudentForm): Boolean {
        return try {
            val userId = getAuthenticatedUserId() // Aquí deberías usar FirebaseAuth.getInstance().currentUser?.uid

            val docRef = db.collection("formStudent").document(formId)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                Log.e("Firebase", "Formulario no encontrado")
                return false
            }

            val storedUserId = snapshot.getString("idStudent")
            if (storedUserId != userId) {
                Log.e("Firebase", "No autorizado para actualizar este formulario")
                return false
            }

            val dataMap = hashMapOf<String, Any>().apply {
                put("idCard", studentData.dataCardId.toInt())
                put("weightedAverage", studentData.dataAverage.toInt())
                put("degree", studentData.dataDegree)
                put("digitsCard", studentData.dataLastDigits.toInt())
                put("shift", studentData.dataShifts.toInt())
                put("semester", studentData.dataSemesterOperator.toInt())
                put("psychology", studentData.dataNamePsychology)
                put("urlApplicationForm", studentData.dataUploadPdf)
                put("scheduleAvailability", studentData.datatableScheduleAvailability.map { daySchedule ->
                    hashMapOf(
                        "day" to daySchedule.day,
                        "shifts" to daySchedule.shifts
                    )
                })
            }

            docRef.set(dataMap, SetOptions.merge()).await()
            true

        } catch (e: Exception) {
            Log.e("Firebase", "Error al actualizar: ${e.message}", e)
            false
        }
    }

    suspend fun getFormOperator(idFormOperator: String?): DataClass? {
        return try {
            if (idFormOperator == null) return null
            val doc = db.collection("formOperator").document(idFormOperator).get().await()
            if(doc.exists()){
                val formOperator = FormOperator(
                    nameForm = doc.getString("nameForm") ?: "",
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

    suspend fun getUserMessages(): List<getMessage> {
        val userId = getAuthenticatedUserId()

        val notificationsRef = db.collection("message")
            .document(userId)
            .collection("notifications")

        val querySnapshot = notificationsRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val formatter = SimpleDateFormat("d/M/yyyy h:mm a", Locale("es", "MX"))
        formatter.timeZone = TimeZone.getTimeZone("America/Mexico_City")

        return querySnapshot.documents.mapNotNull { doc ->
            try {
                val subject = doc.getString("subject") ?: ""
                val message = doc.getString("message") ?: ""
                val date = doc.getTimestamp("timestamp")?.toDate()
                val formattedTimestamp = date?.let { formatter.format(it) } ?: ""
                val status = doc.getLong("status")?.toInt() ?: 0

                getMessage(subject, message, formattedTimestamp, status)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun markMessagesAsSeen() {
        val userId = getAuthenticatedUserId()

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
            pdfRef.putFile(pdfUri).await()
            val downloadUrl = pdfRef.downloadUrl.await()
            return downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Error al subir el archivo: ${e.message}")
        }
    }

    // Para FormOperatorData
    suspend fun getFormOperatorData(): FormOperatorData? {
        return try {
            val doc = db.collection("formOperator")
                .whereEqualTo("activityStatus", 1)
                .limit(1)
                .get()
                .await()

            doc.documents.firstOrNull()?.let { document ->
                FormOperatorData(
                    urlApplicationForm = document.getString("urlApplicationForm") ?: "",
                    iud = document.id,
                    nameForm = document.getString("nameForm") ?: "",
                    semester = document.getString("semester") ?: "",
                    year = document.getLong("year")?.toString() ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error getting operator data: ${e.message}")
            null
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

    suspend fun getActiveForms(): List<formOperatorActive> {
        return try {
            val snapshot = db.collection("formOperator")
                .whereEqualTo("activityStatus", 1) // Solo filtro por estado activo
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                // Extracción directa de datos (sin validar fechas)
                val operatorId = doc.id
                val nameForm = doc.getString("nameForm") ?: return@mapNotNull null
                val semester = doc.getString("semester") ?: return@mapNotNull null
                val year = doc.get("year")?.toString() ?: return@mapNotNull null

                formOperatorActive(
                    operatorIdForm = operatorId,
                    nameActiveForm = nameForm,
                    semesterActive = "$semester $year"
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener formularios activos: ${e.message}")
            emptyList()
        }
    }

    suspend fun getSolicitudes(): List<Solicitud> {
        val idFormOperator = getFormOperatorData()?.iud ?: run {
            Log.d("DEBUG", "No hay formulario operador activo")
            return emptyList()
        }

        Log.d("DEBUG", "Buscando solicitudes para formulario operador: $idFormOperator")

        return try {
            val formStudents = db.collection("formStudent")
                .whereEqualTo("idFormOperator", idFormOperator)
                .get()
                .await()
            Log.d("DEBUG", "Encontrados ${formStudents.size()} formularios llenados por estudiantes")

            if (formStudents.isEmpty) {
                return emptyList()
            }

            // Construir una lista: (formStudentId, idStudent)
            val solicitudesInfo = formStudents.documents.mapNotNull { doc ->
                val idStudent = doc.getString("idStudent")
                if (idStudent != null) {
                    Triple(doc.id, idStudent, doc)
                } else null
            }

            // Obtener información de usuarios por sus IDs
            val studentIds = solicitudesInfo.map { it.second }.distinct()
            val usersSnapshot = db.collection("users")
                .whereIn(FieldPath.documentId(), studentIds)
                .get()
                .await()

            val userMap = usersSnapshot.documents.associateBy { it.id }

            // 4. Mapear a objetos Solicitud
            solicitudesInfo.map { (formStudentId, studentId, formStudentDoc) ->
                val userDoc = userMap[studentId]
                Solicitud(
                    nombre = userDoc?.getString("name") ?: "Sin nombre",
                    correo = userDoc?.getString("email") ?: "Sin email",
                    uidForm = idFormOperator,
                    idFormStudent = formStudentId,
                    carnet = formStudentDoc.get("idCard")?.toString() ?: "Sin carnet",
                    estado = formStudentDoc.get("statusApplicationForm")?.toString() ?: "Sin estado",
                    carrera = formStudentDoc.getString("degree") ?: "Sin carrera",
                    numeroSemestreOperador = formStudentDoc.get("semester")?.toString() ?: "Sin semestre"
                    //idStudent = studentId
                ).also {
                    Log.d("DEBUG", "Solicitud procesada: $it")
                }
            }

        } catch (e: Exception) {
            Log.e("DEBUG", "Error al obtener solicitudes: ${e.message}", e)
            emptyList()
        }
    }

    fun createFormularioOperador(formulario: FormOperador, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("formOperator")
            .add(formulario)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    suspend fun getAllFormOperators(): List<FormOperador> {
        return try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val threshold = currentYear - 4

            // 1. Obtener todos los formularios
            val snapshot = db.collection("formOperator").get().await()

            // 2. Eliminar formularios con año menor al umbral
            for (doc in snapshot.documents) {
                val year = doc.getLong("year")?.toInt()
                if (year != null && year < threshold) {
                    doc.reference.delete()
                }
            }

            // 3. Filtrar válidos y ordenar por fecha de creación (más reciente primero)
            snapshot.documents
                .mapNotNull { doc ->
                    val year = doc.getLong("year")?.toInt()
                    if (year != null && year >= threshold) {
                        doc.toObject(FormOperador::class.java)
                    } else null
                }
                .sortedByDescending { it.createdDate?.toDate() }
        } catch (e: Exception) {
            Log.e("Provider", "Error al obtener formularios: ${e.message}")
            emptyList()
        }
    }

    suspend fun getFormOperator(nameForm: String, semester: String, year: Int): FormOperador? {
        return try {
            Log.d("DEBUG_QUERY", "Buscando -> nameForm: '$nameForm', semester: '$semester', year: $year")

            val query = db.collection("formOperator")
                .whereEqualTo("nameForm", nameForm)
                .whereEqualTo("semester", semester)
                .whereEqualTo("year", year)
                .get()
                .await()

            Log.d("DEBUG_QUERY", "Resultados: ${query.documents.size}")
            query.documents.firstOrNull()?.toObject(FormOperador::class.java)
        } catch (e: Exception) {
            Log.e("DEBUG_QUERY", "Error: ${e.message}")
            null
        }
    }

    suspend fun updateFormOperator(
        nameForm: String,
        semester: String,
        year: Int,
        updatedData: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val query = db.collection("formOperator")
                .whereEqualTo("nameForm", nameForm.trim())
                .whereEqualTo("semester", semester.trim())
                .whereEqualTo("year", year)
                .get()
                .await()

            val doc = query.documents.firstOrNull()
            if (doc != null) {
                db.collection("formOperator").document(doc.id)
                    .update(updatedData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            } else {
                onFailure(Exception("Formulario no encontrado"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun deletePdfFromStorage(url: String) {
        try {
            val storageRef = Firebase.storage.getReferenceFromUrl(url)
            storageRef.delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error al eliminar archivo", e)
        }
    }

    suspend fun uploadPdfToStorage(fileName: String): String {
        val storageRef = Firebase.storage.reference.child(fileName)
        //val uploadTask = storageRef.putFile(uri).await()
        val downloadUrl = storageRef.downloadUrl.await()
        return downloadUrl.toString()
    }


    //Obtener los formularios que ha enviado el estudiante
    suspend fun getInfoStudentForm(): List<FormListStudent> {
        val id = getAuthenticatedUserId()
        return try {
            val snapshot = db.collection("formStudent")
                .whereEqualTo("idStudent", id)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val formStudentDocId = doc.id
                val formId = doc.getString("idFormOperator") ?: return@mapNotNull null

                val listIdInfo = db.collection("formOperator").document(formId).get().await()
                if (!listIdInfo.exists()) return@mapNotNull null

                val semester = listIdInfo.getString("semester") ?: ""
                val year = listIdInfo.get("year") ?.toString()?: ""
                val startDateObj = listIdInfo.getTimestamp("startDate")?.toDate()
                val closingDateObj = listIdInfo.getTimestamp("closingDate")?.toDate()

                val currentDate = Date()
                val isEditable = startDateObj != null && closingDateObj != null &&
                        currentDate.after(startDateObj) && currentDate.before(closingDateObj)

                val closingDateFormatted = closingDateObj?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: ""

                val startDateFormatted = startDateObj?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: ""


                FormListStudent(
                    FormIdStudent = formStudentDocId,
                    FormId = formId,
                    Semester = "$semester $year",
                    FormName = listIdInfo.getString("nameForm") ?: "",
                    DateEnd = closingDateFormatted,
                    DateStart = startDateFormatted,
                    IsEdit = isEditable
                )
            }

        }catch (e: Exception){
            Log.e("FirestoreProvider", "Error al obtener getInfoStudentForm: ${e.message}")
            emptyList()
        }
    }

    fun deleteFormStudent(docId: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("formStudent")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Documento eliminado correctamente")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al eliminar el documento", e)
                onResult(false)
            }
    }

    suspend fun getUserInformation(): UserInformation? {
        val uid = getAuthenticatedUserId()
        val docRef = db.collection("users").document(uid)

        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: ""
                val surnames = snapshot.getString("surnames") ?: ""
                val fullName = "$name $surnames".trim()
                val rolNumerico = snapshot.getLong("userRole")?.toInt() ?: 0
                val rolTexto = when (rolNumerico) {
                    1 -> "Administrador"
                    2 -> "Estudiante"
                    3 -> "Operador"
                    else -> "Desconocido"
                }

                UserInformation(
                    nameUser = fullName,
                    rolUser = rolTexto
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Error al obtener datos del usuario", e)
            null
        }
    }

    suspend fun checkFormSubmission(formId: String): Boolean {
        val studentId = getAuthenticatedUserId()
        return try {
            val query = db.collection("formStudent")
                .whereEqualTo("idStudent", studentId)
                .whereEqualTo("idFormOperator", formId)
                .get()
                .await()

            !query.isEmpty
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al verificar envío de formulario", e)
            false
        }
    }
    //Si el formulario es aceptado cambia el estado de estudiante a operador
     fun registrarNuevoOperador(
        userId: String,
        formId: String,
        nombreUsuario: String,
        correoUsuario: String,
        nombreFormulario: String,
        semestre: String
    ) {
        val operador = hashMapOf(
            "userId" to userId,
            "formId" to formId,
            "nombreUsuario" to nombreUsuario,
            "correoUsuario" to correoUsuario,
            "nombreFormulario" to nombreFormulario,
            "semestre" to semestre,
            "fechaRegistro" to com.google.firebase.Timestamp.now()
        )

        Firebase.firestore.collection("historialOperadores").add(operador)
    }

    //Obtener el horario asignado del operador, validar si es rol 3
    suspend fun getAssignedSchedule():DataClass?{
        val operatorId = getAuthenticatedUserId()
        val rol=getUserInformation()
        return try {
            if(rol?.rolUser!="Operador"){
                val doc = db.collection("").document(operatorId).get().await()
                if(doc.exists()){
                    //Agregar extraccion de datos cuando Dinarte tenga lista la asignación
                    return null
                }else null
            }else null
        }catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos para $operatorId: ${e.message}")
            null
        }
    }

}
