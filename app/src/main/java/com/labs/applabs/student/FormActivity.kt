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

        val buttonURL : Button = findViewById(R.id.buttonURL)

        buttonURL.setOnClickListener {
            Log.e("Click", "Click")
            lifecycleScope.launch {
                val form = provider.getFormOperatorData()
                if (form != null) {
                    downloadBoleta(form.urlApplicationForm!!)
                }
            }
        }
    }

    fun downloadBoleta(urlApplication: String) {
        val btnDescargar = findViewById<FrameLayout>(R.id.btnDescargarBoleta)
        btnDescargar.setOnClickListener {
            if (urlApplication.isNotEmpty()) {
                val request = DownloadManager.Request(Uri.parse(urlApplication))
                    .setTitle("Descargando documento")
                    .setDescription("Formulario PDF")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalFilesDir(
                        this,
                        Environment.DIRECTORY_DOWNLOADS,
                        "formulario.pdf"
                    )

                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
                toastMessage("Descarga iniciada", ToastType.SUCCESS)
            } else {
                toastMessage("No se encontró la URL del documento", ToastType.ERROR)
            }
        }
    }

    fun Siguiente(view: View){
        val intent : Intent = Intent(this@FormActivity, com.labs.applabs.student.FormStudent::class.java)
        startActivity(intent);
    }

}