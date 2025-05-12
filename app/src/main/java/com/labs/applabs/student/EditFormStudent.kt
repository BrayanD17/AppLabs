package com.labs.applabs.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.ScheduleItem
import com.labs.applabs.firebase.editDataStudentForm
import com.labs.applabs.firebase.listSchedule
import kotlinx.coroutines.launch

class EditFormStudent : AppCompatActivity() {
    private lateinit var dataCardId: EditText
    private lateinit var dataAverage:EditText
    private lateinit var dataDegree:Spinner
    private lateinit var dataLastDigits:EditText
    private lateinit var dataShifts:EditText
    private lateinit var dataSemesterOperator:EditText
    private lateinit var dataNamePsychology:EditText
    private lateinit var anyUpload: TextView
    private lateinit var btnUploadPdf: LinearLayout
    private var currentPdfUrl: String? = null
    private val provider: Provider = Provider()

    private companion object {
        private val daysMap = mapOf(
            "Lunes" to Triple(R.id.mondayMorning, R.id.mondayAfternoon, R.id.mondayEvening),
            "Martes" to Triple(R.id.tuesdayMorning, R.id.tuesdayAfternoon, R.id.tuesdayEvening),
            "Miércoles" to Triple(R.id.wednesdayMorning, R.id.wednesdayAfternoon, R.id.wednesdayEvening),
            "Jueves" to Triple(R.id.thursdayMorning, R.id.thursdayAfternoon, R.id.thursdayEvening),
            "Viernes" to Triple(R.id.fridayMorning, R.id.fridayAfternoon, R.id.fridayEvening),
            "Sábado" to Triple(R.id.saturdayMorning, R.id.saturdayAfternoon, R.id.saturdayEvening),
            "Domingo" to Triple(R.id.sundayMorning, R.id.sundayAfternoon, R.id.sundayEvening)
        )
    }

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_form_student)
        showDataStudent()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showDataStudent() {
        dataCardId = findViewById(R.id.editFormDataCardId)
        dataAverage = findViewById(R.id.editFormDataAverage)
        dataDegree = findViewById(R.id.editDataDegree) // Spinner
        dataLastDigits = findViewById(R.id.editDataLastDigits)
        dataShifts = findViewById(R.id.editDataShifts)
        dataSemesterOperator = findViewById(R.id.editDataSemesterOperator)
        dataNamePsychology = findViewById(R.id.editDataNamePsychology)
        anyUpload = findViewById(R.id.anyUpload)
        btnUploadPdf = findViewById(R.id.btnUploadPdf)

        lifecycleScope.launch {
            val careers = provider.getCareerNames()

            val adapter = ArrayAdapter(this@EditFormStudent, android.R.layout.simple_spinner_item, careers
            ).apply {setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)}

            dataDegree.adapter = adapter

            val dataFormStudent = provider.getFormStudent("LBnb7LT7Pu2YTMz4CdJG")
            dataFormStudent?.let { form ->
                val studentInfo = form.studentInfo
                dataCardId.setText(studentInfo.studentId)
                dataAverage.setText(studentInfo.studentAverage)

                // Seleccionar la carrera actual en el Spinner (si existe en la lista)
                studentInfo.studentCareer.let { currentDegree ->
                    val position = careers.indexOfFirst { it == currentDegree }
                    if (position >= 0) {
                        dataDegree.setSelection(position)
                    }
                }

                dataLastDigits.setText(studentInfo.studentLastDigitCard)
                dataShifts.setText(studentInfo.studentShifts)
                dataSemesterOperator.setText(studentInfo.studentSemester)
                dataNamePsychology.setText(studentInfo.namePsycologist)
                getScheduleAvailability(studentInfo.scheduleAvailability)
                // Mostrar nombre del archivo si existe
                currentPdfUrl = form.studentInfo.urlApplication
                if (!currentPdfUrl.isNullOrEmpty()) {
                    val fileName = currentPdfUrl?.substringAfterLast('/') ?.substringBefore("?") ?: "Archivo actual"
                    anyUpload.text = fileName
                }
                btnUploadPdf.setOnClickListener {
                    uploadPdf()
                }

            }
        }
    }

    private fun getScheduleAvailability(schedule: List<ScheduleItem>) {
        schedule.forEach { item ->
            val (morningId, afternoonId, eveningId) = daysMap[item.day] ?: return@forEach

            val morningCheckBox = findViewById<CheckBox>(morningId)
            val afternoonCheckBox = findViewById<CheckBox>(afternoonId)
            val eveningCheckBox = findViewById<CheckBox>(eveningId)

            morningCheckBox.isChecked = item.shift.contains("7am a 12pm")
            afternoonCheckBox.isChecked = item.shift.contains("12pm a 5pm")
            eveningCheckBox.isChecked = item.shift.contains("5pm a 10pm")
        }
    }

    private fun updateDataStudent(newUrl: String) {
        val formId = "LBnb7LT7Pu2YTMz4CdJG"

        val scheduleAvailability = daysMap.mapNotNull { (day, triple) ->
            val (morningId, afternoonId, eveningId) = triple

            val morningCheckBox = findViewById<CheckBox>(morningId)
            val afternoonCheckBox = findViewById<CheckBox>(afternoonId)
            val eveningCheckBox = findViewById<CheckBox>(eveningId)

            val selectedShifts = mutableListOf<String>()
            if (morningCheckBox.isChecked) selectedShifts.add("7am a 12pm")
            if (afternoonCheckBox.isChecked) selectedShifts.add("12pm a 5pm")
            if (eveningCheckBox.isChecked) selectedShifts.add("5pm a 10pm")

            if (selectedShifts.isNotEmpty()) {
                listSchedule(day, selectedShifts)
            } else {
                null
            }
        }

        val studentData = editDataStudentForm(
            dataCardId = dataCardId.text.toString(),
            dataAverage = dataAverage.text.toString(),
            dataDegree = dataDegree.selectedItem?.toString() ?: "",
            dataLastDigits = dataLastDigits.text.toString(),
            dataShifts = dataShifts.text.toString(),
            dataSemesterOperator = dataSemesterOperator.text.toString(),
            dataNamePsychology = dataNamePsychology.text.toString(),
            datatableScheduleAvailability = scheduleAvailability,
            dataUploadPdf = newUrl
        )

        lifecycleScope.launch {
            val success = provider.updateStudentData(formId, studentData)
            if (success) {
                Toast.makeText(this@EditFormStudent, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@EditFormStudent, "Error al actualizar los datos", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val PDF_PICKER_REQUEST_CODE = 123

    private fun uploadPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PDF_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Mostrar nombre del archivo seleccionado
                val fileName = getFileName(uri)
                anyUpload.text = fileName

                // Subir el archivo a Firebase Storage
                lifecycleScope.launch {
                    uploadFileToFirebase(uri)
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = cursor.getString(displayNameIndex)
                }
            }
        }
        return result
    }

    private suspend fun uploadFileToFirebase(uri: Uri) {
        try {
            // 1. Eliminar archivo anterior si existe
            currentPdfUrl?.let { oldUrl ->
                provider.deletePdfFromStorage(oldUrl)
            }

            // 2. Subir nuevo archivo
            val newUrl = provider.uploadPdfToStorage(uri, "formularios/${System.currentTimeMillis()}.pdf")

            // 3. Actualizar URL en Firestore
            updateDataStudent(newUrl)

            // Actualizar UI y variable local
            currentPdfUrl = newUrl
            Toast.makeText(this, "Archivo actualizado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al subir archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("UploadPDF", "Error: ${e.message}")
        }
    }

}