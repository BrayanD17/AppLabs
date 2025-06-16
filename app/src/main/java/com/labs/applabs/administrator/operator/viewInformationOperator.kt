package com.labs.applabs.administrator.operator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
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
    private lateinit var operatorActiveId: String
    private var operatorName: String = ""
    val colors = listOf("#FF6F61", "#6B5B95", "#88B04B", "#F7CAC9", "#92A8D1")

    private companion object {
        private val daysMap = mapOf(
            "Lunes" to Triple(R.id.mondayMorningOperator, R.id.mondayAfternoonOperator,R.id.mondayEveningOperator),
            "Martes" to Triple(R.id.tuesdayMorningOperator, R.id.tuesdayAfternoonOperator, R.id.tuesdayEveningOperator),
            "Miércoles" to Triple(R.id.wednesdayMorningOperator, R.id.wednesdayAfternoonOperator, R.id.wednesdayEveningOperator),
            "Jueves" to Triple(R.id.thursdayMorningOperator, R.id.thursdayAfternoonOperator, R.id.thursdayEveningOperator),
            "Viernes" to Triple(R.id.fridayMorningOperator, R.id.fridayAfternoonOperator, R.id.fridayEveningOperator),
            "Sábado" to Triple(R.id.saturdayMorningOperator, R.id.saturdayAfternoonOperator, R.id.saturdayEveningOperator),
            "Domingo" to Triple(R.id.sundayMorningOperator, R.id.sundayAfternoonOperator, R.id.sundayEveningOperator)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_information_operator)

        val id = intent.getStringExtra("operatorId")
        if (id == null) {
            toastMessage("ID del operador no recibido", ToastType.ERROR)
            finish()
            return
        }
        operatorActiveId = id
        getOperatorInfo(operatorActiveId)

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
                loadLaboratories()
                toggleLabSelection()
            }
        }

    }

    fun getOperatorInfo(operatorId: String) {
        val operatorGetName = findViewById<TextView>(R.id.textDataNameOperator)
        val operatorIdCard = findViewById<TextView>(R.id.textDataIdOperator)
        val operatorEmail = findViewById<TextView>(R.id.textDataEmailOperator)
        val operatorPhone = findViewById<TextView>(R.id.textDataPhoneOperator)

        lifecycleScope.launch {
            val infoUser = provider.getUserInfo(operatorId)

            infoUser?.let { info ->
                val studentInfo = info.studentInfo
                operatorName = "${studentInfo.studentName} ${studentInfo.surNames}"
                operatorGetName.text= operatorName
                operatorIdCard.text = studentInfo.studentCard
                operatorEmail.text = studentInfo.studentEmail
                operatorPhone.text = studentInfo.studentCard
            }
        }
    }

    private fun toggleLabSelection() {
        if (labSelectionContainer.visibility == View.VISIBLE) {
            labSelectionContainer.visibility = View.GONE
            btnEditSchedule.text = getString(R.string.editSchedule)
            btnEditSchedule.backgroundTintList= getColorStateList(R.color.green)
        } else {
            labSelectionContainer.visibility = View.VISIBLE
            btnEditSchedule.text = getString(R.string.cancel)
            btnEditSchedule.backgroundTintList= getColorStateList(R.color.red)
        }
    }

    private suspend fun loadLaboratories() {
        try {
            val laboratories = provider.getLaboratoryName()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@viewInformationOperator, android.R.layout.simple_spinner_item, laboratories).apply {setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                laboratorySpinner.adapter = adapter
                laboratorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        btnConfirmLab.visibility = View.VISIBLE
                        btnConfirmLab.backgroundTintList= getColorStateList(R.color.green)
                        btnConfirmLab.background=getDrawable(R.drawable.bg_button)
                        btnConfirmLab.setOnClickListener {
                            Toast.makeText(this@viewInformationOperator, "Laboratorio seleccionado: ${laboratories[position]}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        btnConfirmLab.visibility = View.GONE
                    }
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@viewInformationOperator, "Error al cargar los laboratorios: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Laboratorios", "Error: ${e.message}")
            }
        }
    }

    fun backViewInformationOperator(view: android.view.View) {
        finish()
    }
}