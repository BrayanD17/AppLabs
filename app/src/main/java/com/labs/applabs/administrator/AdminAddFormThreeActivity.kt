package com.labs.applabs.administrator

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.labs.applabs.R
import com.labs.applabs.utils.StepIndicatorActivity
import java.text.SimpleDateFormat
import java.util.Locale

class AdminAddFormThreeActivity : StepIndicatorActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_form_three)
        updateStepIndicator(2)

        val etRequerimiento = findViewById<EditText>(R.id.etRequerimiento)
        val btnAgregarRequerimiento = findViewById<Button>(R.id.btnAgregarRequerimiento)
        val listaRequerimientos = findViewById<LinearLayout>(R.id.listaRequerimientos)
        val tvTituloReq = findViewById<TextView>(R.id.tvTituloListaRequerimientos)

        val etBeneficio = findViewById<EditText>(R.id.etBeneficio)
        val btnAgregarBeneficio = findViewById<Button>(R.id.btnAgregarBeneficio)
        val listaBeneficios = findViewById<LinearLayout>(R.id.listaBeneficios)
        val tvTituloBen = findViewById<TextView>(R.id.tvTituloListaBeneficios)

        val nombreFormulario = intent.getStringExtra("nombreFormulario") ?: ""
        val periodo = intent.getStringExtra("periodo") ?: ""
        val linkFormulario = intent.getStringExtra("linkFormulario") ?: ""
        val fechaHabilitado = intent.getStringExtra("fechaHabilitado") ?: ""
        val fechaCierre = intent.getStringExtra("fechaCierre") ?: ""
        val fechaCreacion = intent.getStringExtra("fechaCreacion") ?: ""
        val anioActual = intent.getIntExtra("anioActual", 0)

        btnAgregarRequerimiento.setOnClickListener {
            val texto = etRequerimiento.text.toString().trim()
            if (texto.isNotEmpty()) {
                tvTituloReq.visibility = View.VISIBLE
                val item = crearItem(texto, listaRequerimientos, tvTituloReq)
                listaRequerimientos.addView(item)
                etRequerimiento.text.clear()
            } else {
                Toast.makeText(this, "Escribe un requerimiento", Toast.LENGTH_SHORT).show()
            }
        }

        btnAgregarBeneficio.setOnClickListener {
            val texto = etBeneficio.text.toString().trim()
            if (texto.isNotEmpty()) {
                tvTituloBen.visibility = View.VISIBLE
                val item = crearItem(texto, listaBeneficios, tvTituloBen)
                listaBeneficios.addView(item)
                etBeneficio.text.clear()
            } else {
                Toast.makeText(this, "Escribe un beneficio", Toast.LENGTH_SHORT).show()
            }
        }

        val btnSave = findViewById<Button>(R.id.btnSaveForm)
        btnSave.setOnClickListener {
            val requerimientos = getTextListFromLinearLayout(listaRequerimientos)
            val beneficios = getTextListFromLinearLayout(listaBeneficios)
            val requisitosYBeneficios = requerimientos + beneficios

            if (requisitosYBeneficios.isEmpty()) {
                Toast.makeText(this, "Debe agregar al menos un requerimiento o beneficio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timestampCreacion = com.google.firebase.Timestamp(sdf.parse(fechaCreacion)!!)
            val timestampHabilitado = com.google.firebase.Timestamp(sdf.parse(fechaHabilitado)!!)
            val timestampCierre = com.google.firebase.Timestamp(sdf.parse(fechaCierre)!!)

            val formulario = com.labs.applabs.operador.FormOperador(
                activityStatus = 1,
                closingDate = timestampCierre,
                createdDate = timestampCreacion,
                nameForm = nombreFormulario,
                requirementsAndBenefits = requisitosYBeneficios,
                semester = periodo,
                startDate = timestampHabilitado,
                urlApplicationForm = linkFormulario,
                year = anioActual
            )

            // Subir a Firebase
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("formOperator")
                .add(formulario)
                .addOnSuccessListener {
                    Toast.makeText(this, "Formulario guardado exitosamente.", Toast.LENGTH_LONG).show()
                    finish() // o volver a la pantalla principal
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

    }

    private fun getTextListFromLinearLayout(layout: LinearLayout): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until layout.childCount) {
            val row = layout.getChildAt(i) as? LinearLayout
            val textView = row?.getChildAt(0) as? TextView
            textView?.text?.toString()?.let { if (it.isNotEmpty()) list.add(it) }
        }
        return list
    }


    private fun crearItem(texto: String, lista: LinearLayout, titulo: TextView): LinearLayout {
        val context = this

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
            background = ContextCompat.getDrawable(context, R.drawable.card_background)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 12, 0, 0)
            layoutParams = params
            elevation = 4f
        }

        val tv = TextView(context).apply {
            text = texto
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.textColor))
            typeface = ResourcesCompat.getFont(context, R.font.montserrat_light)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnEliminar = Button(ContextThemeWrapper(context, R.style.ButtonDeleteStyle), null, 0).apply {
            text = "Eliminar"
            setOnClickListener {
                lista.removeView(container)
                if (lista.childCount == 0) {
                    titulo.visibility = View.GONE
                }
            }
        }

        container.addView(tv)
        container.addView(btnEliminar)
        return container
    }

}
