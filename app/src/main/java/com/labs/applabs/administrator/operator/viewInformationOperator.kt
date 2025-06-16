package com.labs.applabs.administrator.operator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class viewInformationOperator : AppCompatActivity() {

    private val provider: Provider = Provider()
    private lateinit var labSelectionContainer: LinearLayout
    private lateinit var laboratorySpinner: Spinner
    private lateinit var btnConfirmLab: Button
    private lateinit var btnEditSchedule: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_information_operator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        labSelectionContainer = findViewById(R.id.labSelectionContainer)
        laboratorySpinner = findViewById(R.id.laboratorySpinner)
        btnConfirmLab = findViewById(R.id.btnConfirmLab)
        btnEditSchedule = findViewById(R.id.btnEditScheduleOperatorSaveChage)

        // Configurar el botón de edición
        btnEditSchedule.setOnClickListener {
            lifecycleScope.launch {
                loadLaboratories() // Carga los datos
                toggleLabSelection() // Muestra el contenedor del Spinner
            }
        }

    }

    private fun toggleLabSelection() {
        if (labSelectionContainer.visibility == View.VISIBLE) {
            labSelectionContainer.visibility = View.GONE
            btnEditSchedule.text = getString(R.string.editSchedule)
        } else {
            labSelectionContainer.visibility = View.VISIBLE
            btnEditSchedule.text = getString(R.string.cancel)
        }
    }

    private suspend fun loadLaboratories() {
        try {
            // Paso 1: Obtener los datos del provider
            val laboratories = provider.getLaboratoryName() // ← Aquí llamas a tu función

            // Paso 2: Configurar el Spinner en el hilo principal
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@viewInformationOperator, android.R.layout.simple_spinner_item, laboratories).apply {setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                laboratorySpinner.adapter = adapter

                // Listener para mostrar el botón de confirmación
                laboratorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        btnConfirmLab.visibility = View.VISIBLE
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        btnConfirmLab.visibility = View.GONE
                    }
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                //Toast.makeText(cont, "Error cargando laboratorios", Toast.LENGTH_SHORT).show()
                Log.e("Laboratorios", "Error: ${e.message}")
            }
        }
    }

    fun backViewInformationOperator(view: android.view.View) {
        finish()
    }
}