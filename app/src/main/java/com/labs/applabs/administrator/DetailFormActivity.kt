package com.labs.applabs.administrator

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R
import com.labs.applabs.firebase.provider

class DetailFormActivity : AppCompatActivity() {

    private lateinit var applicationOperatorTitle: TextView
    private lateinit var typeForm:TextView
    val provider: provider = provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_form)
        showInfo()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun showInfo(){
        applicationOperatorTitle=findViewById(R.id.textViewApplicationTitle)
        typeForm=findViewById(R.id.textViewTypeForm)

        applicationOperatorTitle.text="Solicitud IS 2025"
        typeForm.text="Formulario Operador"
    }



}