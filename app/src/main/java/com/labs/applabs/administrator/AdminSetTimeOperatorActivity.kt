package com.labs.applabs.administrator

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import com.labs.applabs.models.ScheduleSelection
import kotlinx.coroutines.launch

class AdminSetTimeOperatorActivity : AppCompatActivity() {

    private val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    private val turnos = listOf("Mañana", "Tarde", "Noche")

    // [operadorId] -> [labId] -> ScheduleSelection
    private val operatorSchedules = mutableMapOf<String, MutableMap<String, ScheduleSelection>>()
    private lateinit var spnLab: Spinner
    private lateinit var spnOp: Spinner
    private lateinit var tableLayout: TableLayout
    private var currentLab: String = ""
    private var selectedOperatorId: String? = null

    private var operatorList: List<Provider.OperatorUser> = listOf()
    private var labsList: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_set_time_operator)

        val infoIcon: ImageView = findViewById(R.id.infoIcon)
        infoIcon.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Horarios de turnos")
                .setMessage(
                    "Mañana: 7:00 am - 12:00 md\n" +
                            "Tarde: 12:00 md - 5:00 pm\n" +
                            "Noche: 5:00 pm - 10:00 pm"
                )
                .setPositiveButton("Entendido", null)
                .show()
        }

        spnLab = findViewById(R.id.spnLab)
        spnOp = findViewById(R.id.spnOp)
        tableLayout = findViewById(R.id.tableLayout)

        lifecycleScope.launch {
            // Carga la estructura completa desde Firebase al iniciar
            val provider = Provider()
            val loadedSchedules = provider.cargarAssignSchedulesDesdeFirebase()
            operatorSchedules.clear()
            loadedSchedules.forEach { (k, v) -> operatorSchedules[k] = v.toMutableMap() }
        }

        // Llenar laboratorios
        lifecycleScope.launch {
            labsList = Provider().getLaboratoryNames()
            val adapter = ArrayAdapter(
                this@AdminSetTimeOperatorActivity,
                android.R.layout.simple_spinner_item,
                labsList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnLab.adapter = adapter
        }

        // Llenar operadores
        lifecycleScope.launch {
            val provider = Provider()
            val approvedPairs = provider.getApprovedOperatorFormIds()
            val activeStudentIds = provider.filterActiveOperatorStudents(approvedPairs)
            operatorList = provider.getOperatorNamesById(activeStudentIds)
            val adapter = ArrayAdapter(
                this@AdminSetTimeOperatorActivity,
                android.R.layout.simple_spinner_item,
                operatorList.map { it.fullName }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnOp.adapter = adapter
        }

        // Cambia de operador
        spnOp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val operador = operatorList[position]
                selectedOperatorId = operador.id
                val scheduleMap = operatorSchedules.getOrPut(operador.id) { mutableMapOf() }
                val labId = currentLab.takeIf { it.isNotEmpty() } ?: labsList.getOrNull(0) ?: ""
                currentLab = labId
                val schedule = scheduleMap[labId] ?: ScheduleSelection(mutableMapOf())
                mostrarTablaHorario(schedule)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Cambia de laboratorio
        spnLab.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val labId = parent.getItemAtPosition(position).toString()
                currentLab = labId
                val operadorId = selectedOperatorId ?: return
                val scheduleMap = operatorSchedules.getOrPut(operadorId) { mutableMapOf() }
                val schedule = scheduleMap[labId] ?: ScheduleSelection(mutableMapOf())
                mostrarTablaHorario(schedule)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val btnSaveShedule: Button = findViewById(R.id.btnSaveSchedule)
        btnSaveShedule.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val provider = Provider()
                    provider.guardarAssignSchedulesEnFirebase(operatorSchedules)
                    Toast.makeText(this@AdminSetTimeOperatorActivity, "Horarios guardados correctamente", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@AdminSetTimeOperatorActivity, "Error al guardar los horarios", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    /**
     * Muestra la tabla, bloqueando solo los turnos (día, franja) que ya fueron asignados en otros labs
     * al mismo operador
     */
    private fun mostrarTablaHorario(schedule: ScheduleSelection) {
        val operadorId = selectedOperatorId ?: return

        for (i in diasSemana.indices) {
            val fila = tableLayout.getChildAt(i + 1) as TableRow
            for (j in turnos.indices) {
                val cb = fila.getChildAt(j + 1) as CheckBox
                val dia = diasSemana[i]
                val turno = turnos[j]
                cb.setOnCheckedChangeListener(null)
                cb.isChecked = schedule.days[dia]?.contains(turno) == true

                val bloquear = debeBloquearCheckbox(operadorId, currentLab, dia, turno)
                cb.isEnabled = !bloquear

                cb.tag = Triple(currentLab, dia, turno)

                cb.setOnCheckedChangeListener { _, isChecked ->
                    onHorarioCheckboxChanged(currentLab, dia, turno, isChecked)
                }
            }
        }
    }

    /**
     * Marca o desmarca turnos para el operador actual
     */
    private fun onHorarioCheckboxChanged(labId: String, dia: String, turno: String, checked: Boolean) {
        val operadorId = selectedOperatorId ?: return
        val scheduleMap = operatorSchedules.getOrPut(operadorId) { mutableMapOf() }
        val schedule = scheduleMap.getOrPut(labId) { ScheduleSelection(mutableMapOf()) }
        val turnosSet = schedule.days.getOrPut(dia) { mutableSetOf() }
        if (checked) {
            turnosSet.add(turno)
        } else {
            turnosSet.remove(turno)
            if (turnosSet.isEmpty()) schedule.days.remove(dia)
        }
        scheduleMap[labId] = schedule
        operatorSchedules[operadorId] = scheduleMap

        // Refresca la tabla para actualizar bloqueos
        mostrarTablaHorario(schedule)
    }

    /**
     * Indica si este checkbox debe estar bloqueado para el operador y laboratorio actual
     */
    private fun debeBloquearCheckbox(
        operadorId: String,
        labId: String,
        dia: String,
        turno: String
    ): Boolean {
        // 1. Bloquea si OTRO operador ya tiene este (día, turno) en este laboratorio
        for ((otherOperadorId, scheduleMap) in operatorSchedules) {
            if (otherOperadorId == operadorId) continue
            val schedule = scheduleMap[labId] ?: continue
            val turnosSet = schedule.days[dia] ?: continue
            if (turnosSet.contains(turno)) {
                return true
            }
        }
        // 2. Bloquea si ESTE operador ya tiene este (día, turno) en OTRO laboratorio
        val scheduleMap = operatorSchedules[operadorId] ?: return false
        for ((otherLabId, schedule) in scheduleMap) {
            if (otherLabId == labId) continue
            val turnosSet = schedule.days[dia] ?: continue
            if (turnosSet.contains(turno)) {
                return true
            }
        }
        // Si no está bloqueado por ninguna de las reglas, está disponible
        return false
    }
}
