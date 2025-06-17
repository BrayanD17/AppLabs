package com.labs.applabs.administrator.operator

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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
    private lateinit var userId: String
    private var operatorName: String = ""

    private val colors = listOf("#FF6F61", "#6B5B95", "#88B04B", "#F7CAC9", "#92A8D1")

    private companion object {
        private val daysMap = mapOf(
            "Lunes" to Triple(R.id.mondayMorningOperator, R.id.mondayAfternoonOperator, R.id.mondayEveningOperator),
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

        //Initialize elements
        labSelectionContainer = findViewById(R.id.labSelectionContainer)
        laboratorySpinner = findViewById(R.id.laboratorySpinner)
        btnConfirmLab = findViewById(R.id.btnConfirmLab)
        btnEditSchedule = findViewById(R.id.btnEditScheduleOperatorSaveChage)

        val type = intent.getStringExtra("type")
        if (type == "show") {
            val id = intent.getStringExtra("userId")
            if (id == null) {
                toastMessage("ID del operador no recibido", ToastType.ERROR)
                finish(); return
            }
            userId = id
            operatorActiveId = userId
            getOperatorInfo(userId)
            btnEditSchedule.visibility = View.GONE
        } else {
            val id = intent.getStringExtra("operatorId")
            if (id == null) {
                toastMessage("ID del operador no recibido", ToastType.ERROR)
                finish(); return
            }
            operatorActiveId = id
            getOperatorInfo(operatorActiveId)
        }

        //Data load activity
        setCheckboxesEnabled(false)
        showAllLabsReadOnly()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom); insets
        }

        //btn edit and cancel for edit schedule
        btnEditSchedule.setOnClickListener {
            lifecycleScope.launch {
                loadLaboratories()
                toggleLabSelection()
            }
        }
    }

    //Get information of operator
    private fun getOperatorInfo(operatorId: String) {
        val tvName  = findViewById<TextView>(R.id.textDataNameOperator)
        val tvCard  = findViewById<TextView>(R.id.textDataIdOperator)
        val tvMail  = findViewById<TextView>(R.id.textDataEmailOperator)
        val tvPhone = findViewById<TextView>(R.id.textDataPhoneOperator)

        lifecycleScope.launch {
            provider.getUserInfo(operatorId)?.let { info ->
                val s = info.studentInfo
                operatorName = "${s.studentName} ${s.surNames}"
                tvName.text  = operatorName
                tvCard.text  = s.studentCard
                tvMail.text  = s.studentEmail
                tvPhone.text = s.studentCard
            }
        }
    }

    //Helpers for view information assigned schedule according select
    private fun setCheckboxesEnabled(enabled: Boolean) {
        daysMap.values.forEach { (m, a, e) ->
            listOf(m, a, e).forEach { id ->
                findViewById<CheckBox>(id).isEnabled = enabled
            }
        }
    }

    private fun toggleLabSelection() {
        val enteringEdit = labSelectionContainer.visibility != View.VISIBLE
        labSelectionContainer.visibility = if (enteringEdit) View.VISIBLE else View.GONE

        btnEditSchedule.text = if (enteringEdit) getString(R.string.cancel) else getString(R.string.editSchedule)
        btnEditSchedule.backgroundTintList = getColorStateList(if (enteringEdit) R.color.red else R.color.green)

        if (!enteringEdit) {
            //Exit editing full view and locked checkboxes
            showAllLabsReadOnly()
        }
    }

    //Manage spinner options
    private suspend fun loadLaboratories() {
        try {
            val labs = provider.getLaboratoryName().toMutableList().apply { add(0, "Visualizar horario") }
            withContext(Dispatchers.Main) {
                laboratorySpinner.adapter = ArrayAdapter(this@viewInformationOperator, android.R.layout.simple_spinner_item, labs).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                laboratorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val sel = labs[position]
                        if (sel == "Visualizar horario") {
                            showAllLabsReadOnly()
                            btnConfirmLab.visibility = View.GONE
                        } else {
                            showOnlyThisLab(sel)
                            btnConfirmLab.visibility = View.VISIBLE
                            btnConfirmLab.backgroundTintList = getColorStateList(R.color.green)
                            btnConfirmLab.background = getDrawable(R.drawable.bg_button)
                            btnConfirmLab.setOnClickListener {
                                updateDataScheduleAssigned(operatorActiveId, sel)
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                toastMessage("Error al cargar laboratorios: ${e.message}", ToastType.ERROR)
                Log.e("Laboratorios", "Error: ${e.message}")
            }
        }
    }

    //Get and show schedule assigned
    private fun showOnlyThisLab(lab: String) {
        lifecycleScope.launch {
            val raw = withContext(Dispatchers.IO) { provider.getAssignedScheduleForOperator(operatorActiveId) }
            val filtered = parseScheduleData(raw).filter { it.labName == lab }
            displayScheduleTable(filtered)
            displayAssignedLabsOnly(filtered)
            setCheckboxesEnabled(true)
        }
    }

    private fun showAllLabsReadOnly() {
        lifecycleScope.launch {
            val raw = withContext(Dispatchers.IO) { provider.getAssignedScheduleForOperator(operatorActiveId) }
            val all = parseScheduleData(raw)
            displayScheduleTable(all)
            displayAssignedLabsOnly(all)
            setCheckboxesEnabled(false)
        }
    }

    //Parse list schedule from send data a firebase
    private fun parseScheduleData(data: Map<String, Any>?): List<LabSchedule> {
        val list = mutableListOf<LabSchedule>()
        val labsMap = data?.get("labs") as? Map<String, Map<String, Any>> ?: return list
        labsMap.forEach { (labName, daysMap) ->
            val days = mutableMapOf<String, List<String>>()
            daysMap.forEach { (day, shifts) ->
                val s = when (shifts) {
                    is String   -> listOf(shifts)
                    is List<*>  -> shifts.filterIsInstance<String>()
                    else        -> emptyList()
                }
                days[day] = s
            }
            list.add(LabSchedule(labName, days))
        }
        return list
    }

    //Draw table
    private fun displayScheduleTable(schedules: List<LabSchedule>) {
        daysMap.values.forEach { (m, a, e) -> listOf(m, a, e).forEach { resetCheckBox(it) } }

        schedules.forEachIndexed { idx, lab ->
            val color = colors[idx % colors.size]
            lab.days.forEach { (dayName, shifts) ->
                val key = when (dayName.lowercase()) {
                    "lunes"       -> "Lunes"
                    "martes"      -> "Martes"
                    "miercoles", "miércoles" -> "Miércoles"
                    "jueves"      -> "Jueves"
                    "viernes"     -> "Viernes"
                    "sabado", "sábado" -> "Sábado"
                    "domingo"     -> "Domingo"
                    else           -> dayName
                }
                val triple = daysMap[key] ?: return@forEach
                val (mId, aId, eId) = triple
                shifts.forEach { s ->
                    when {
                        s.contains("mañana", true) -> setCheckBoxColor(mId, color, lab.labName)
                        s.contains("tarde", true)  -> setCheckBoxColor(aId, color, lab.labName)
                        s.contains("noche", true)  -> setCheckBoxColor(eId, color, lab.labName)
                    }
                }
            }
        }
    }

    private fun resetCheckBox(id: Int) {
        findViewById<CheckBox>(id).apply {
            isChecked = false
            tag = null
            buttonTintList = null
        }
    }

    private fun setCheckBoxColor(id: Int, color: String, lab: String) {
        findViewById<CheckBox>(id).apply {
            isChecked = true
            buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            tag = lab
        }
    }

    //Load the assigned labs
    private fun displayAssignedLabsOnly(schedules: List<LabSchedule>) {
        val container = findViewById<LinearLayout>(R.id.containerInformationCheckBoxes)
        container.removeAllViews()
        schedules.forEachIndexed { idx, lab ->
            val color = colors[idx % colors.size]
            container.addView(CheckBox(this).apply {
                text = lab.labName
                isChecked = true
                isClickable = false
                textAlignment = LinearLayout.TEXT_ALIGNMENT_TEXT_START
                setTextAppearance(this@viewInformationOperator, R.style.formMessageStyle)
                buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener { highlightLabSchedule(lab.labName, color) }
            })
        }
    }

    private fun highlightLabSchedule(lab: String, color: String) {
        daysMap.values.forEach { (m, a, e) -> listOf(m, a, e).forEach { resetCheckBox(it) } }
        daysMap.values.forEach { (m, a, e) ->
            listOf(m, a, e).forEach { id ->
                val cb = findViewById<CheckBox>(id)
                if (cb.tag == lab) cb.buttonTintList = ColorStateList.valueOf(Color.parseColor(color))
            }
        }
    }

    //Save changes of every assigned schedule in the lab select
    private fun updateDataScheduleAssigned(operatorId: String, labName: String) {
        lifecycleScope.launch {
            try {
                val newLabSchedule = buildLabScheduleFromUI(labName)
                val current = withContext(Dispatchers.IO) { provider.getAssignedScheduleForOperator(operatorId) }
                val merged = mergeSchedules(parseScheduleData(current), newLabSchedule) ?: run {
                    toastMessage("Ese turno ya está asignado a otro laboratorio", ToastType.ERROR); return@launch
                }
                val ok = withContext(Dispatchers.IO) { provider.updateOperatorSchedule(operatorId, merged) }
                if (ok) {
                    toastMessage("Horario actualizado correctamente", ToastType.SUCCESS)
                    val filtered = merged.filter { it.labName == labName }
                    displayScheduleTable(filtered)
                    displayAssignedLabsOnly(filtered)
                    toggleLabSelection()
                } else {
                    toastMessage("No se pudo actualizar el horario", ToastType.ERROR)
                }
            } catch (e: Exception) {
                Log.e("updateSchedule", "Error: ${e.message}")
                toastMessage("Error al actualizar horario", ToastType.ERROR)
            }
        }
    }

    private fun buildLabScheduleFromUI(lab: String): LabSchedule {
        val days = mutableMapOf<String, List<String>>()
        daysMap.forEach { (day, triple) ->
            val (mId, aId, eId) = triple
            val shifts = mutableListOf<String>()
            if (findViewById<CheckBox>(mId).isChecked) shifts.add("Mañana")
            if (findViewById<CheckBox>(aId).isChecked) shifts.add("Tarde")
            if (findViewById<CheckBox>(eId).isChecked) shifts.add("Noche")
            if (shifts.isNotEmpty()) days[day] = shifts
        }
        return LabSchedule(lab, days)
    }

    private fun mergeSchedules(current: List<LabSchedule>, updated: LabSchedule): List<LabSchedule>? {
        val merged = current.toMutableList().apply {
            val idx = indexOfFirst { it.labName == updated.labName }
            if (idx >= 0) this[idx] = updated else add(updated)
        }
        val seen = mutableSetOf<String>()
        for (ls in merged) for ((day, shifts) in ls.days) for (shift in shifts) {
            val key = "${day.lowercase()}-${shift.lowercase()}"
            if (!seen.add(key)) return null
        }
        return merged
    }

    fun backViewInformationOperator(view: View) { finish() }
}
