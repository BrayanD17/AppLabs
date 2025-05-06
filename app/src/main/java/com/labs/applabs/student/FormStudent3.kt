package com.labs.applabs.student

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch


class FormStudent3 : AppCompatActivity() {

    private lateinit var semesters: EditText
    private lateinit var psychology: EditText
    var ticket : String = "boleta.pdf"
    val provider: Provider= Provider()

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

        // Restaurar datos si existen
        if (FormStudentData.idCard.toString().isNotEmpty()) {
            semesters.setText(FormStudentData.semester.toString())
            psychology.setText(FormStudentData.psychology)
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

        return true
    }

    private fun saveFormData(): Boolean {
        return try {
            FormStudentData.semester = semesters.text.toString()
            FormStudentData.psychology = psychology.text.toString()
            FormStudentData.ticketUrl = ticket
            true
        } catch (e: Exception) {
            Toast.makeText(this, R.string.errorSavingData, Toast.LENGTH_SHORT).show()
            false
        }
    }


    fun Next(view: View) {
        lifecycleScope.launch {
            if (validateFields() && saveFormData()) {
                val saved = provider.saveStudentData(FormStudentData)
                if (saved) {
                    startActivity(Intent(this@FormStudent3, FormActivity::class.java))
                } else {
                    Toast.makeText(this@FormStudent3, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@FormStudent3, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun Back(view: View) {
        finish()
    }

}