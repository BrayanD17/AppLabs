package com.labs.applabs.administrator

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.provider
import kotlinx.coroutines.launch

class DetailFormActivity : AppCompatActivity() {

    private lateinit var applicationOperatorTitle: TextView
    private lateinit var typeForm:TextView
    val provider: provider = provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_form)
        showInfo("UUli6leBpZyt5GFvHp7o")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun showInfo(formularioId: String) {
        applicationOperatorTitle = findViewById(R.id.textViewApplicationTitle)
        typeForm = findViewById(R.id.textViewTypeForm)


        // Ejecutar en corrutina
        lifecycleScope.launch {
            val info = provider.getFormularioInfoById(formularioId)

            if (info != null) {
                val nombre = info["nombre"] as? String ?: "Nombre no disponible"
                val semestre = info["Semestre"] as? String ?: "Semestre no disponible"

                applicationOperatorTitle.text = "$nombre"
                typeForm.text = "$semestre"
            } else {
                applicationOperatorTitle.text = "No se encontró información"
                typeForm.text = ""
            }
        }

       val container = findViewById<LinearLayout>(R.id.containerData)

// Lista de ejemplo, puedes reemplazar esto con datos reales de Firebase, Room, etc.
        val horarios = listOf("Lunes 9-11", "Martes 14-16", "Viernes 10-12", "Viernes 10-12","Viernes 10-12","Viernes 10-12","Viernes 10-12")
        val tipoLetra = ResourcesCompat.getFont(this, R.font.montserrat_light)
        for (horario in horarios) {
            val textView = TextView(this).apply {
                text = horario
                textSize = 14f
                typeface = tipoLetra
                setPadding(0, 8, 0, 8)
            }
            container.addView(textView)
        }


    }




}