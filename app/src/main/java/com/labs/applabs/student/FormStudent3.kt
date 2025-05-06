package com.labs.applabs.student

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
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
import android.provider.OpenableColumns


class FormStudent3 : AppCompatActivity() {

    private lateinit var semesters: EditText
    private lateinit var psychology: EditText
    private lateinit var card : CardView
    val provider: Provider= Provider()
    private val PICK_PDF_REQUEST = 1
    private var selectedPdfUri: Uri? = null


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedPdfUri = data.data

            val fileName = getFileNameFromUri(this ,selectedPdfUri!!)
            findViewById<TextView>(R.id.tvPdfFileName).text = "Seleccionado: $fileName"
        }
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

        if (selectedPdfUri == null) {
            Toast.makeText(this, "Debe seleccionar un archivo PDF", Toast.LENGTH_SHORT).show()
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

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return result ?: uri.path?.substringAfterLast('/') ?: "Archivo PDF"
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
                try {
                    if (selectedPdfUri != null) {
                        val url = provider.uploadPdfToFirebase(selectedPdfUri!!)
                        FormStudentData.ticketUrl = url
                    }
                    val saved = provider.saveStudentData(FormStudentData)
                    if (saved) {
                        startActivity(Intent(this@FormStudent3, FormActivity::class.java))
                        toastMessage("Formulario guardado exitosamente", ToastType.SUCCESS)
                    } else {
                        toastMessage("Error al guardar formulario", ToastType.ERROR)
                    }
                } catch (e: Exception) {
                    toastMessage("Error al subir el PDF: ${e.message}", ToastType.ERROR)
                }
            }
        }
    }

    fun Back(view: View) {
        finish()
    }

}