package com.labs.applabs.operadores

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch
import org.apache.poi.ss.formula.functions.T

class Report_Misconduct_Activity : AppCompatActivity() {

    val provider : Provider = Provider()
    private lateinit var laboratory: Spinner
    private lateinit var student: AutoCompleteTextView
    private lateinit var semester: Spinner
    private lateinit var comment: EditText
    private lateinit var sendReport: Button
    private lateinit var selectedLaboratory: String
    private lateinit var selectedStudent: String
    private lateinit var selectedSemester: String


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
                    // provider.sendReportMisconduct(selectedLaboratory, selectedStudent, selectedSemester, comment.text.toString())
                    toastMessage("Reporte enviado", ToastType.SUCCESS)
                    finish()
                } catch (e : Exception){
                    toastMessage("Error al enviar el reporte", ToastType.ERROR)
                }
            }
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
                val studentList = provider.getLaboratoryName()
                val adapter = ArrayAdapter(this@Report_Misconduct_Activity, android.R.layout.simple_dropdown_item_1line, studentList)
                student.setAdapter(adapter)
                student.setOnItemClickListener { parent, view, position, id ->
                    selectedStudent = studentList[position]
                }
            } catch (e : Exception){
                toastMessage("Error al cargar los datos", ToastType.ERROR)
            }
        }
    }


    private fun loadSpinnerSemester(){
        lifecycleScope.launch {
            try {
                val studentList = provider.getLaboratoryName()
                val adapter = ArrayAdapter(this@Report_Misconduct_Activity, android.R.layout.simple_dropdown_item_1line, studentList)
                student.setAdapter(adapter)
                student.setOnItemClickListener { parent, view, position, id ->
                    selectedStudent = studentList[position]
                }
            } catch (e : Exception){
                toastMessage("Error al cargar los datos", ToastType.ERROR)
            }
        }
    }


}