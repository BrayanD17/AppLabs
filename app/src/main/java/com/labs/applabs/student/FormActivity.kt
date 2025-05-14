package com.labs.applabs.student

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
    private val provider : Provider = Provider()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val button = findViewById<Button>(R.id.buttonURL)
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

    fun nextForm(view: View){
        val intent : Intent = Intent(this@FormActivity, com.labs.applabs.student.FormStudent::class.java)
        startActivity(intent);
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