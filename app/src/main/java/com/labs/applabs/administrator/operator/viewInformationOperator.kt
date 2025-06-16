package com.labs.applabs.administrator.operator

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
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
import com.labs.applabs.firebase.LabSchedule
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
        setCheckboxesEnabled(false)
        showDataScheduleAssigned(operatorActiveId)

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

    private fun setCheckboxesEnabled(enabled: Boolean) {
        daysMap.values.forEach { (morningId, afternoonId, eveningId) ->
            findViewById<android.widget.CheckBox>(morningId).isEnabled = enabled
            findViewById<android.widget.CheckBox>(afternoonId).isEnabled = enabled
            findViewById<android.widget.CheckBox>(eveningId).isEnabled = enabled
        }
    }

    private fun toggleLabSelection() {
        val isEditing = labSelectionContainer.visibility != View.VISIBLE
        labSelectionContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
        btnEditSchedule.text = if (isEditing) getString(R.string.cancel) else getString(R.string.editSchedule)
        btnEditSchedule.backgroundTintList = getColorStateList(if (isEditing) R.color.red else R.color.green)

        //Active checkbox
        setCheckboxesEnabled(isEditing)
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
                            updateDataScheduleAssigned(operatorActiveId, laboratories[position])
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

    //Show data schedule assigned and laboratories assigned
    private fun showDataScheduleAssigned(operatorId: String){
        lifecycleScope.launch {
            try {
                val scheduleData = provider.getAssignedScheduleForOperator(operatorId)
                if (scheduleData != null) {
                    val labSchedules = parseScheduleData(scheduleData)
                    displayScheduleTable(labSchedules)
                    displayAssignedLabsOnly(labSchedules)
                } else {
                    Log.d("viewAssignedSchedule", "No se encontraron horarios asignados")
                }
            } catch (e: Exception) {
                Log.e("viewAssignedSchedule", "Error al cargar horarios: ${e.message}")
            }
        }
    }

    private fun parseScheduleData(data: Map<String, Any>?): List<LabSchedule> {
        val labSchedules = mutableListOf<LabSchedule>()

        data?.let { scheduleData ->
            val labsMap = scheduleData["labs"] as? Map<String, Map<String, Any>> ?: return labSchedules

            labsMap.forEach { (labName, daysMap) ->
                val days = mutableMapOf<String, List<String>>()

                daysMap.forEach { (day, shifts) ->
                    val shiftsList = when (shifts) {
                        is String -> listOf(shifts)
                        is List<*> -> shifts.filterIsInstance<String>()
                        else -> emptyList()
                    }
                    days[day] = shiftsList
                }
                labSchedules.add(LabSchedule(labName, days))
            }
        }

        return labSchedules
    }

    private fun displayScheduleTable(labSchedules: List<LabSchedule>) {
        viewInformationOperator.daysMap.values.forEach { (morningId, afternoonId, eveningId) ->
            resetCheckBox(morningId)
            resetCheckBox(afternoonId)
            resetCheckBox(eveningId)
        }

        labSchedules.forEachIndexed { labIndex, labSchedule ->
            val color = colors.getOrElse(labIndex % colors.size) { "#000000" }

            labSchedule.days.forEach { (dayName, shifts) ->
                val dayKey = when (dayName.lowercase()) {
                    "lunes" -> "Lunes"
                    "martes" -> "Martes"
                    "miercoles", "miércoles" -> "Miércoles"
                    "jueves" -> "Jueves"
                    "viernes" -> "Viernes"
                    "sabado", "sábado" -> "Sábado"
                    "domingo" -> "Domingo"
                    else -> dayName
                }

                val (morningId, afternoonId, eveningId) = viewInformationOperator.daysMap[dayKey] ?: return@forEach

                shifts.forEach { shift ->
                    when {
                        shift.contains("mañana", ignoreCase = true) ->
                            setCheckBoxColor(morningId, color, labSchedule.labName)
                        shift.contains("tarde", ignoreCase = true) ->
                            setCheckBoxColor(afternoonId, color, labSchedule.labName)
                        shift.contains("noche", ignoreCase = true) ->
                            setCheckBoxColor(eveningId, color, labSchedule.labName)
                    }
                }
            }
        }
    }

    private fun resetCheckBox(checkBoxId: Int) {
        val checkBox = findViewById<CheckBox>(checkBoxId)
        checkBox.isChecked = false
        checkBox.tag = null
    }

    private fun setCheckBoxColor(checkBoxId: Int, color: String, labName: String) {
        val checkBox = findViewById<CheckBox>(checkBoxId)
        checkBox.isChecked = true
        checkBox.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
        checkBox.tag = labName
    }

    private fun displayAssignedLabsOnly(labSchedules: List<LabSchedule>) {
        val container = findViewById<LinearLayout>(R.id.containerInformationCheckBoxes)
        container.removeAllViews()

        labSchedules.forEachIndexed { index, labSchedule ->
            val color = colors.getOrElse(index % colors.size) { "#000000" }
            val checkBox = CheckBox(this).apply {
                text = labSchedule.labName
                isChecked = true
                isClickable = false
                textAlignment = LinearLayout.TEXT_ALIGNMENT_TEXT_START
                setTextAppearance(this@viewInformationOperator, R.style.formMessageStyle)
                buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    highlightLabSchedule(labSchedule.labName, color)
                }
            }
            container.addView(checkBox)
        }
    }

    private fun highlightLabSchedule(labName: String, color: String) {
        viewInformationOperator.daysMap.values.forEach { (morningId, afternoonId, eveningId) ->
            resetCheckBox(morningId)
            resetCheckBox(afternoonId)
            resetCheckBox(eveningId)
        }

        viewInformationOperator.daysMap.values.forEach { (morningId, afternoonId, eveningId) ->
            val morningCheck = findViewById<CheckBox>(morningId)
            val afternoonCheck = findViewById<CheckBox>(afternoonId)
            val eveningCheck = findViewById<CheckBox>(eveningId)

            if (morningCheck.tag == labName) {
                morningCheck.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            }
            if (afternoonCheck.tag == labName) {
                afternoonCheck.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            }
            if (eveningCheck.tag == labName) {
                eveningCheck.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            }
        }
    }

    //Update data schedule assigned
    private fun updateDataScheduleAssigned(operatorId: String, laboratoryName: String){
        lifecycleScope.launch {

        }
    }

    fun backViewInformationOperator(view: android.view.View) {
        finish()
    }
}