package com.labs.applabs.student

import android.app.Activity
import android.app.ComponentCaller
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class FormActivity : AppCompatActivity() {


    private val formStudentLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish() // Cierra esta activity si el formulario fue enviado correctamente
        }
    }

    private val provider : Provider = Provider()
    private lateinit var formIdActive: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        finishActivity()
        
        val formName : TextView = findViewById(R.id.tvForm)
        val formSemester : TextView = findViewById(R.id.tvSemester)
        formName.text = intent.getStringExtra("formName")
        formSemester.text = intent.getStringExtra("semesterForm")


    }

    private fun downloadBoleta(urlApplication: String) {
        val fileName = Uri.parse(urlApplication).lastPathSegment?.substringAfterLast("/")?.substringBefore("?") ?: "archivo.pdf"
        val request = DownloadManager.Request(Uri.parse(urlApplication))
            .setTitle("Descargando documento")
            .setDescription(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(
                this,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        toastMessage("Descarga iniciada", ToastType.SUCCESS)
    }

    fun nextForm(view: View) {
        val id = intent.getStringExtra("formIdFormActive")
        if (id == null) {
            toastMessage("ID de formulario no recibido", ToastType.ERROR)
            finish()
            return
        }
        formIdActive = id

        // Verificar si el estudiante ya ha enviado este formulario
        lifecycleScope.launch {
            try {
                val alreadySubmitted = provider.checkFormSubmission(formIdActive)

                if (alreadySubmitted) {
                    toastMessage( "Formulario enviado. Edítalo desde 'Formularios enviados' si es necesario.", ToastType.ERROR)
                } else {
                    FormStudentData.idFormOperator = formIdActive
                    val intent = Intent(this@FormActivity, FormStudent::class.java)
                    formStudentLauncher.launch(intent)
                }
            } catch (e: Exception) {
                Log.e("FormActivity", "Error al verificar envío de formulario", e)
                toastMessage("Error al verificar formulario", ToastType.ERROR)
            }
        }
    }

    private fun finishActivity(){
        val backView = findViewById<ImageView>(R.id.backViewFormActivyty)
        backView.setOnClickListener {
            finish()
        }
    }

    fun onClickFormButton(view: View) {
        Log.d("DEBUG", "Botón del formulario presionado")

        lifecycleScope.launch {
            try {
                val formData = provider.getFormOperatorData()
                val formUrl = formData?.urlApplicationForm
                Log.d("DEBUG", "Link de descarga: $formUrl")
                downloadBoleta(formUrl!!)
            } catch (e: Exception) {
                Log.e("DEBUG", "Error al obtener el formulario", e)
                toastMessage("Error al descargar la boleta", ToastType.ERROR)
            }
        }
    }


}