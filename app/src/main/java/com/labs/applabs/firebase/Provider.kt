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
import com.labs.applabs.models.ScheduleSelection
import com.labs.applabs.models.Usuario
import com.labs.applabs.administrator.operator.OperadorCompleto
import com.labs.applabs.student.FormStudentData
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar
import kotlin.math.log
import kotlinx.coroutines.tasks.await as await1
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
            val snapshot = db.collection("dataDefault").document("careers").get().await1()
            snapshot.get("career") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("Firebase", "Error al cargar carreras", e)
            emptyList()
        }
    }

    suspend fun getFormStatusData() : List<String> {
        return try{
            val snapshot = db.collection("dataDefault").document("statusForm").get().await1()
            snapshot.get("status") as? List<String> ?: emptyList()
        } catch (e : Exception){
            Log.e("Firebase", "Error al cargar estados", e)
            emptyList()
        }
    }

    suspend fun getLaboratoryName() : List<String> {
        return try{
            val laboratories = db.collection("dataDefault").document("laboratories").get().await1()
            laboratories.get("laboratory") as? List<String> ?: emptyList()
        } catch (e : Exception){
            Log.e("Firebase", "Error al cargar laboratorios", e)
            emptyList()
        }
    }

    suspend fun getApprovedOperatorFormIds(): List<Pair<String, String>> {
        return try {
            val snapshot = db.collection("formStudent")
                .whereEqualTo("comment", "Aprobado")
                .get().await1()
            snapshot.documents.mapNotNull { doc ->
                val idStudent = doc.getString("idStudent")
                val idFormOperator = doc.getString("idFormOperator")
                if (idStudent != null && idFormOperator != null) Pair(idStudent, idFormOperator) else null
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error al obtener estudiantes aprobados", e)
            emptyList()
        }
    }

    suspend fun filterActiveOperatorStudents(pairs: List<Pair<String, String>>): List<String> {
        val approvedStudents = mutableListOf<String>()
        try {
            for ((idStudent, idFormOperator) in pairs) {
                val doc = db.collection("formOperator").document(idFormOperator).get().await1()
                val status = doc.getLong("activityStatus") ?: 0
                if (status == 1L) {
                    approvedStudents.add(idStudent)
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error filtrando operadores activos", e)
        }
        return approvedStudents
    }

    data class OperatorUser(val id: String, val fullName: String)

    suspend fun getOperatorNamesById(ids: List<String>): List<OperatorUser> {
        val operatorNames = mutableListOf<OperatorUser>()
        try {
            for (id in ids) {
                val doc = db.collection("users").document(id).get().await1()
                val userRole = doc.getLong("userRole")
                if (userRole == 3L) {
                    val name = doc.getString("name") ?: ""
                    val surnames = doc.getString("surnames") ?: ""
                    operatorNames.add(OperatorUser(id, "$name $surnames".trim()))
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error obteniendo nombres de operadores", e)
        }
        return operatorNames
    }
    suspend fun getStudentName() : Map<String, String> {
        return try{
            val students = db.collection("users").whereEqualTo("userRole", 2).get().await1()
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
        val historial = db.collection("operatorHistory").get().await1()
        val lista = mutableListOf<OperadorCompleto>()

        for (doc in historial.documents) {
            val userId = doc.getString("userId") ?: continue
            val formId = doc.getString("formId") ?: continue

            // Fetch usuario
            val userDoc = db.collection("users").document(userId).get().await()

            // Verificar si el usuario tiene rol de operador (3)
            val userRole = userDoc.getLong("userRole")?.toInt() ?: continue
            if (userRole != 3) continue

            val studentCard = userDoc.getString("studentCard") ?: ""
            val name = userDoc.getString("name") + " " + (userDoc.getString("surnames") ?: "")
            val email = userDoc.getString("email") ?: ""

            // Fetch formStudent
            val formDoc = db.collection("formStudent").document(formId).get().await1()
            val degree = formDoc.getString("degree") ?: ""

            // Fetch assignSchedule (laboratorios y horarios)
            val asignDoc = db.collection("assignSchedule").document(userId).get().await1()
            val labs: MutableMap<String, Map<String, List<String>>> = mutableMapOf()
            val labsMap = asignDoc.get("labs") as? Map<*, *>
            if (labsMap != null) {
                for ((labName, diasObj) in labsMap) {
                    val diasMap = diasObj as? Map<*, *> ?: continue
                    val dias = mutableMapOf<String, List<String>>()
                    for ((dia, horarioValue) in diasMap) {
                        val listaHorarios = when (horarioValue) {
                            is String -> listOf(horarioValue)
                            is List<*> -> horarioValue.filterIsInstance<String>()
                            else -> emptyList()
                        }
                        dias[dia.toString()] = listaHorarios
                    }
                    labs[labName.toString()] = dias
                }
            }

            lista.add(
                OperadorCompleto(
                    userId = userId,
                    carnet = studentCard,
                    nombre = name,
                    carrera = degree,
                    correo = email,
                    laboratorios = labs
                )
            )
        }
        return lista
    }

    suspend fun guardarAssignSchedulesEnFirebase(operatorSchedules: Map<String, Map<String, ScheduleSelection>>) {
        val batch = db.batch()
        for ((userId, labsMap) in operatorSchedules) {
            // Construir estructura labs
            val labs = mutableMapOf<String, MutableMap<String, List<String>>>()
            for ((labName, schedule) in labsMap) {
                for ((dia, turnos) in schedule.days) {
                    labs.getOrPut(labName) { mutableMapOf() }[dia] = turnos.toList()
                }
            }
            val docRef = db.collection("assignSchedule").document(userId)
            batch.set(docRef, mapOf("userId" to userId, "labs" to labs))
        }
        batch.commit().await1()
    }

    suspend fun cargarAssignSchedulesDesdeFirebase(): Map<String, Map<String, ScheduleSelection>> {
        val snapshot = db.collection("assignSchedule").get().await1()
        val result = mutableMapOf<String, MutableMap<String, ScheduleSelection>>()

        for (doc in snapshot.documents) {
            val userId = doc.getString("userId") ?: continue
            val labsMap = mutableMapOf<String, ScheduleSelection>()
            val labsObj = doc.get("labs") as? Map<*, *> ?: continue
            for ((labNameAny, daysAny) in labsObj) {
                val labName = labNameAny as? String ?: continue
                val daysMap = mutableMapOf<String, MutableSet<String>>()
                val daysObj = daysAny as? Map<*, *> ?: continue
                for ((diaAny, turnosAny) in daysObj) {
                    val dia = diaAny as? String ?: continue
                    val turnosList = (turnosAny as? List<*>)?.mapNotNull { it as? String } ?: continue
                    daysMap[dia] = turnosList.toMutableSet()
                }
                labsMap[labName] = ScheduleSelection(daysMap)
            }
            result[userId] = labsMap
        }
        return result
    }

    suspend fun getUserInfo(userId: String?): DataClass? {
        return try {
            if (userId == null) return null
            val doc = db.collection("users").document(userId).get().await1()
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
                .await1()

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
                .await1()
            return true
        } catch (e : Exception){
            Log.e("Firebase", "Error al guardar el reporte de mala conducta.(${e.message})")
            return false
        }
    }

    //Obtener los datos del estudiente de formStudent por id de usuario
    suspend fun getFormStudent(formId: String): DataClass?  {
        return try {
            val doc = db.collection("formStudent").document(formId).get().await1()
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
            val snapshot = docRef.get().await1()

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

            docRef.set(dataMap, SetOptions.merge()).await1()
            true

        } catch (e: Exception) {
            Log.e("Firebase", "Error al actualizar: ${e.message}", e)
            false
        }
    }

    suspend fun getFormOperator(idFormOperator: String?): DataClass? {
        return try {
            if (idFormOperator == null) return null
            val doc = db.collection("formOperator").document(idFormOperator).get().await1()
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
            ).await1()
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

        notificationsCollection.add(newNotification).await1()
    }

    suspend fun getUserMessages(): List<getMessage> {
        val userId = getAuthenticatedUserId()

        val notificationsRef = db.collection("message")
            .document(userId)
            .collection("notifications")

        val querySnapshot = notificationsRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await1()

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
            .await1()

        //Update each unread message in a batch
        val batch = db.batch()
        querySnapshot.documents.forEach { doc ->
            batch.update(doc.reference, "status", 1)
        }

        if (!querySnapshot.isEmpty) {
            batch.commit().await1()
        }
    }


    // Esta función será suspendida para poder usarse con coroutines
    suspend fun uploadPdfToFirebase(pdfUri: Uri): String {
        val fileName = "${System.currentTimeMillis()}.pdf"
        val pdfRef = storageRef.child(fileName)

        try {
            pdfRef.putFile(pdfUri).await1()
            val downloadUrl = pdfRef.downloadUrl.await1()
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
                .await1()

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
            val token = FirebaseMessaging.getInstance().token.await1()

            db.collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await1()

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
                .await1()

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
                .await1()
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
                .await1()

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
                    estado = formStudentDoc.get("statusApplicationForm")?.toString() ?: "",
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
            val snapshot = db.collection("formOperator").get().await1()

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
                .await1()

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
                .await1()

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
            storageRef.delete().await1()
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error al eliminar archivo", e)
        }
    }

    suspend fun uploadPdfToStorage(fileName: String): String {
        val storageRef = Firebase.storage.reference.child(fileName)
        val downloadUrl = storageRef.downloadUrl.await1()
        return downloadUrl.toString()
    }


    //Obtener los formularios que ha enviado el estudiante
    suspend fun getInfoStudentForm(): List<FormListStudent> {
        val id = getAuthenticatedUserId()
        return try {
            val snapshot = db.collection("formStudent")
                .whereEqualTo("idStudent", id)
                .get()
                .await1()

            snapshot.documents.mapNotNull { doc ->
                val formStudentDocId = doc.id
                val formId = doc.getString("idFormOperator") ?: return@mapNotNull null

                val listIdInfo = db.collection("formOperator").document(formId).get().await1()
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
            val snapshot = docRef.get().await1()
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
                .await1()

            !query.isEmpty
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al verificar envío de formulario", e)
            false
        }
    }
    // Agrega esto dentro de tu clase Provider (puedes ponerlo cerca de otros métodos suspend)
    suspend fun operatorRegister(formId: String) {
        // Cambia estado en formStudent
        db.collection("formStudent").document(formId)
            .update("statusApplicationForm", 1, "comment", "Aprobado")
            .await1()

        // Trae datos del formulario
        val formSnap = db.collection("formStudent").document(formId).get().await1()
        val form = formSnap.data ?: return
        val idStudent = form["idStudent"] as? String ?: return
        val semester = form["semester"]?.toString() ?: ""

        // Busca datos de usuario
        val userSnap = db.collection("users").document(idStudent).get().await1()
        val name = "${userSnap.getString("name") ?: ""} ${userSnap.getString("surnames") ?: ""}".trim()
        val email = userSnap.getString("email") ?: ""

        // Cambia rol usando el id del documento
        db.collection("users").document(idStudent).update("userRole", 3).await1()

        // Agrega/actualiza historial operador
        val operador = hashMapOf(
            "userId" to idStudent,
            "formId" to formId,
            "nombreUsuario" to name,
            "correoUsuario" to email,
            "nombreFormulario" to "Formulario Operador",
            "semestre" to semester,
            "fechaRegistro" to com.google.firebase.Timestamp.now()
        )
        // Usamos una clave única
        db.collection("operatorHistory")
            .document("$formId-$idStudent")
            .set(operador, SetOptions.merge())
            .await1()
    }

    //Get operator assigned schedule data as an operator
    suspend fun getAssignedSchedule(): Map<String, Any>? {
        val operatorId = getAuthenticatedUserId()
        val rol = getUserInformation()
        return try {
            if(rol?.rolUser == "Operador") {
                val doc = db.collection("assignSchedule").document(operatorId).get().await()
                if(doc.exists()) {
                    doc.data
                } else null
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener datos para $operatorId: ${e.message}")
            null
        }
    }

    //Get assigned schedule data as an specific operator (from admin view)
    suspend fun getAssignedScheduleForOperator(operatorId: String): Map<String, Any>? {
        return try {
            val doc = db.collection("assignSchedule").document(operatorId).get().await()
            if (doc.exists()) {
                doc.data
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener horario para $operatorId: ${e.message}")
            null
        }
    }

    //Update schedule assigned operator (admin)
    suspend fun updateOperatorSchedule(operatorId: String, labSchedules: List<LabSchedule>): Boolean {
        return try {
            val labsMap = mutableMapOf<String, Any>()

            labSchedules.forEach { labSchedule ->
                val daysMap = mutableMapOf<String, Any>()
                labSchedule.days.forEach { (day, shifts) ->
                    daysMap[day] = shifts
                }
                labsMap[labSchedule.labName] = daysMap
            }

            val scheduleData = mapOf(
                "userId" to operatorId,
                "labs" to labsMap
            )

            db.collection("assignSchedule").document(operatorId)
                .set(scheduleData)
                .await()

            true
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al actualizar horario: ${e.message}")
            false
        }
    }

    // Obtener el horario asignado general como AssignedScheduleData
    suspend fun obtenerHorariosAsignadosGeneral(): List<AssignedScheduleData> {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val snapshot = db.collection("assignSchedule").get().await1()
        val lista = mutableListOf<AssignedScheduleData>()

        // Carga todos los usuarios solo una vez para no hacer n consultas
        val allUsers = db.collection("users").get().await1().documents.associateBy { it.id }

        fun normalizarDia(dia: String): String = when (dia.trim().lowercase()) {
            "lunes" -> "Lunes"
            "martes" -> "Martes"
            "miércoles" -> "Miércoles"
            "jueves" -> "Jueves"
            "viernes" -> "Viernes"
            "sábado" -> "Sábado"
            else -> dia
        }
        fun normalizarTurno(turno: String): String = when (turno.trim().lowercase()) {
            "mañana" -> "7 a 12"
            "tarde" -> "12 a 5"
            "noche" -> "5 a 10"
            else -> turno
        }

        for (doc in snapshot.documents) {
            val userId = doc.getString("userId") ?: continue
            val labs = doc.get("labs") as? Map<*, *> ?: continue

            // Obtener nombre completo (si no existe pone "Sin nombre")
            val userDoc = allUsers[userId]
            val nombre = (userDoc?.getString("name") ?: "") + " " + (userDoc?.getString("surnames") ?: "")

            for ((labKey, diasMap) in labs) {
                val nombreLab = labKey.toString()
                val diasTurnos = diasMap as? Map<*, *> ?: continue
                for ((dia, turnos) in diasTurnos) {
                    val diaNormal = normalizarDia(dia.toString())
                    val turnosList = turnos as? List<*> ?: continue
                    for (turno in turnosList) {
                        val turnoNormal = normalizarTurno(turno.toString())
                        lista.add(
                            AssignedScheduleData(
                                name = "",
                                laboratory = nombreLab,
                                shift = turnoNormal,
                                day = diaNormal,
                                operator = nombre.trim().ifEmpty { "Sin nombre" },
                                scheduleMatrix = emptyMap()
                            )
                        )
                    }
                }
            }
        }
        return lista
    }



    suspend fun getStudentIdCarne(studentCard : String): String?{
        return try {
            val query = db.collection("users")
                .whereEqualTo("studentCard", studentCard)
                .get()
                .await1()
            return query.documents.firstOrNull()?.id.toString()
        } catch (e : Exception){
            Log.e("FirestoreProvider", "Error: ${e.message}")
            null
        }
    }

    suspend fun saveReportVisitStudent(report: ReportVisitStudent) : Boolean {
        try {
            val user = getAuthenticatedUserId()
            val data = hashMapOf(
                "idOperator" to user,
                "idStudent" to report.idstudent,
                "laboratory" to report.laboratory,
                "date" to report.date,
                "startTime" to report.startTime,
                "endTime" to report.endTime
            )

            db.collection("reportVisit")
                .add(data)
                .await1()
            return true
        } catch (e : Exception){
            Log.e("FirestoreProvider", "Error:  ${e.message}")
            return false
        }
    }


    suspend fun updateEndTime(idStudent: String, targetDate: String): Boolean {
        return try {
            val query = db.collection("reportVisit")
                .whereEqualTo("idStudent", idStudent)
                .whereEqualTo("date", targetDate)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await1()

            if (query.isEmpty) return false

            val mostRecentReport = query.documents[0]
            val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
            val time = LocalTime.now().format(formatterTime).toString()
            mostRecentReport.reference.update("endTime", time).await1()
            Log.d("Firestore", "Hora de salida actualizada correctamente")
            true
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al actualizar hora de salida: ${e.message}", e)
            false
        }
    }

    suspend fun getMisconductStudents(): List<MisconductStudent> {
        return try {
            val misconductSnapshot = db.collection("misconducReportStudent")
                .get()
                .await1()

            val misconductStudents = mutableListOf<MisconductStudent>()

            for (doc in misconductSnapshot.documents) {
                val uid = doc.id
                val semester = doc.getString("semester") ?: continue
                val studentUid = doc.getString("student") ?: continue
                val laboratory = doc.getString("laboratory") ?: continue



                // Buscar el usuario en la colección 'users' con ese UID
                val userSnapshot = db.collection("users")
                    .document(studentUid)
                    .get()
                    .await1()

                val studentName = userSnapshot.getString("name") ?: ""
                val surnames = userSnapshot.getString("surnames") ?: ""
                val fullName = "$studentName $surnames".trim()
                val email = userSnapshot.getString("email") ?: ""
                val studentCard = userSnapshot.getString("studentCard") ?: ""

                misconductStudents.add(
                    MisconductStudent(
                        id = uid,
                        student = fullName,
                        email = email,
                        semester = semester,
                        laboratory = laboratory,
                        cardStudent = studentCard
                    )
                )
            }
            return misconductStudents
        } catch (e: Exception) {
            Log.e("FirestoreProvider", "Error al obtener estudiantes con reporte: ${e.message}")
            emptyList()
        }
    }

    suspend fun getVisitReport(): List<ReportVisit> {
        return try {
            val visitSnapshot = db.collection("reportVisit")
                .get()
                .await1()

            val visitList = mutableListOf<ReportVisit>()

            for (doc in visitSnapshot.documents) {
                val idStudent = doc.getString("idStudent") ?: continue
                val laboratory = doc.getString("laboratory") ?: continue
                val date = doc.getString("date") ?: continue
                val startTime = doc.getString("startTime") ?: continue
                val endTime = doc.getString("endTime") ?: continue

                val userStudent = db.collection("users")
                    .document(idStudent)
                    .get()
                    .await1()
                val studentName = userStudent.getString("name") ?: ""
                val surnames = userStudent.getString("surnames") ?: ""
                val fullName = "$studentName $surnames".trim()
                val cardStudent = userStudent.getString("studentCard") ?: ""

                visitList.add(
                    ReportVisit(
                        student = fullName,
                        cardStudent = cardStudent,
                        laboratory = laboratory,
                        date = date,
                        startTime = startTime,
                        endTime = endTime
                    )
                )
            }

            return visitList
        } catch (e : Exception){
            Log.e("FirestoreProvider", "Error: ${e.message}")
            emptyList()
        }
    }
}