package com.labs.applabs.operator

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.ReportMisconducStudent
import kotlinx.coroutines.launch

class Report_Misconduct_Activity : AppCompatActivity() {

    val provider : Provider = Provider()
    private lateinit var laboratory: AutoCompleteTextView
    private lateinit var student: AutoCompleteTextView
    private lateinit var semester: AutoCompleteTextView
    private lateinit var comment: EditText
    private lateinit var sendReport: Button
    private lateinit var selectedLaboratory: String
    private lateinit var selectedStudentUid: String
    private lateinit var selectedSemester: String
    private lateinit var btnBack : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_misconduct)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        laboratory = findViewById(R.id.spinnerLaboratory)
        student = findViewById(R.id.spinnerStudentMisconduct)
        semester = findViewById(R.id.spinnerSemester)
        comment = findViewById(R.id.editTextCommentMisconduct)
        sendReport = findViewById(R.id.btnSendReport)

        loadSpinnerLaboratory()
        loadSpinnerStudent()
        loadSpinnerSemester()

        sendReport.setOnClickListener {
            lifecycleScope.launch {
                try {
                    if(validateInputs()){
                        val reportMisconducStudent = ReportMisconducStudent(
                            laboratory = selectedLaboratory,
                            student = selectedStudentUid,
                            semester = selectedSemester,
                            comment = comment.text.toString())
                        val result = provider.saveStudentMisconductos(reportMisconducStudent)
                        if(result){
                            toastMessage("Reporte enviado correctamente", ToastType.SUCCESS)
                            finish()
                        }
                    }
                } catch (e : Exception){
                    toastMessage("Error al enviar el reporte", ToastType.ERROR)
                }
            }
        }

        btnBack = findViewById(R.id.backViewFormActivyty)
        btnBack.setOnClickListener {
            finish()
        }


        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

    }

    private fun loadSpinnerLaboratory(){
        lifecycleScope.launch {
            try {
                val laboratoryList = provider.getLaboratoryName()
                val adapter = ArrayAdapter(this@Report_Misconduct_Activity, android.R.layout.simple_dropdown_item_1line, laboratoryList)
                laboratory.setAdapter(adapter)
                laboratory.setOnItemClickListener { parent, view, position, id ->
                    selectedLaboratory = laboratoryList[position]
                }
            } catch (e : Exception){
                toastMessage("Error al cargar los datos", ToastType.ERROR)
            }
        }
    }

    private fun loadSpinnerStudent(){
        lifecycleScope.launch {
            try {
                val studentList = provider.getStudentName()
                val nameStudents = studentList.keys.toList()
                val adapter = ArrayAdapter(this@Report_Misconduct_Activity, android.R.layout.simple_dropdown_item_1line, nameStudents)
                student.setAdapter(adapter)
                student.setOnItemClickListener { parent, view, position, id ->
                    val selectedName = parent.getItemAtPosition(position) as String
                    selectedStudentUid = studentList[selectedName].toString()
                    // Aquí puedes usar el UID como necesites
                    Log.d("Selected", "UID del estudiante: $selectedStudentUid")
                }
            } catch (e : Exception){
                toastMessage("Error al cargar los datos", ToastType.ERROR)
            }
        }
    }


    private fun loadSpinnerSemester(){
        lifecycleScope.launch {
            try {
                val SemesterList = listOf(getString(R.string.semester1), getString(R.string.semester2))
                val adapter = ArrayAdapter(this@Report_Misconduct_Activity, android.R.layout.simple_dropdown_item_1line, SemesterList)
                semester.setAdapter(adapter)
                semester.setOnItemClickListener { parent, view, position, id ->
                    selectedSemester = SemesterList[position]
                }
            } catch (e : Exception){
                toastMessage("Error al cargar los datos", ToastType.ERROR)
            }
        }
    }

    private fun validateInputs() : Boolean{
        if (selectedLaboratory.isEmpty() || selectedStudentUid.isEmpty() || selectedSemester.isEmpty() || comment.text.toString().isEmpty()){
            toastMessage("Por favor llenar todos los datos requeridos", ToastType.ERROR)
            return false
        } else{
            return true
        }
    }

}