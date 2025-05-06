package com.labs.applabs.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch


class FormStudent3 : AppCompatActivity() {

    private lateinit var semesters: EditText
    private lateinit var psychology: EditText
    private lateinit var card : CardView
    val provider: Provider= Provider()
    private val PICK_PDF_REQUEST = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_student3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        semesters = findViewById(R.id.etSemesters)
        psychology = findViewById(R.id.etPsychology)
        card = findViewById(R.id.cardUploadPdf)

        // Restaurar datos si existen
        if (FormStudentData.idCard.toString().isNotEmpty()) {
            semesters.setText(FormStudentData.semester.toString())
            psychology.setText(FormStudentData.psychology)
        }

        card.setOnClickListener { selectPdfFile() }


    }

    private fun validateFields(): Boolean {

        if (semesters.text.toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.errorSemester, Toast.LENGTH_SHORT).show()
            return false
        }

        if (psychology.text.toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.errorPsychology, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveFormData(): Boolean {
        return try {
            FormStudentData.semester = semesters.text.toString()
            FormStudentData.psychology = psychology.text.toString()

            true
        } catch (e: Exception) {
            Toast.makeText(this, R.string.errorSavingData, Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val pdfUri = data.data!!
            // Llamamos a la función suspendida dentro de una coroutine
            lifecycleScope.launch {
                try {
                    val downloadUrl = provider.uploadPdfToFirebase(pdfUri)  // Espera el resultado de la subida
                    FormStudentData.ticketUrl = downloadUrl  // Asigna el URL del archivo subido
                    Log.d("Firebase", "URL del PDF subido: $downloadUrl")
                } catch (e: Exception) {
                    toastMessage("Error al subir el archivo", ToastType.ERROR)
                }
            }
        }
    }


    private fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo PDF"), PICK_PDF_REQUEST)
    }

    fun Next(view: View) {
        lifecycleScope.launch {
            if (validateFields() && saveFormData()) {
                val saved = provider.saveStudentData(FormStudentData)
                if (saved) {
                    startActivity(Intent(this@FormStudent3, FormActivity::class.java))
                    toastMessage("Formulario guardado exitosamente",ToastType.SUCCESS)
                } else {
                    toastMessage("Error al guardar formulario",ToastType.ERROR)
                }
            }
        }
    }


    fun Back(view: View) {
        finish()
    }

}