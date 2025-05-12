package com.labs.applabs.student

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class EditFormStudent : AppCompatActivity() {
    private lateinit var dataCardId: EditText
    private lateinit var dataAverage:EditText
    private lateinit var dataDegree:Spinner
    private lateinit var dataLastDigits:EditText
    private lateinit var dataShifts:EditText
    private lateinit var dataSemesterOperator:EditText
    private lateinit var dataNamePsychology:EditText
    private lateinit var tableScheduleAvailability: TableLayout
    private val provider: Provider = Provider()

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
            }
        }
    }


    /*tableScheduleAvailability
    btnUploadPdf //subir archivo
    anyUpload //Cambiar a archivo subido o seleccionado o al nombre del archivo
    btnEditSaveChange //guardar cambios*/
}