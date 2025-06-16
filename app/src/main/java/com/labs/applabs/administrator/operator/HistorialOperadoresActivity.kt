package com.labs.applabs.administrator.operator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.R
import com.labs.applabs.export.ExportSchedulesActivity
import com.labs.applabs.administrator.operator.Adapter.HistorialOperadoresAdapter
import kotlinx.coroutines.launch
import com.labs.applabs.firebase.Provider
class HistorialOperadoresActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistorialOperadoresAdapter
    private lateinit var searchEditText: EditText
    private lateinit var btnFiltro: ImageView
    private lateinit var tvFiltroAplicado: TextView
    private var filtroActual = TipoFiltro.NOMBRE
    private var carreraSeleccionada: String? = null
    private var operadoresList: List<OperadorCompleto> = emptyList()
    private var provider = Provider()
    private var laboratorioSeleccionado: String? = null
    private var diaSeleccionado: String? = null
    private var horarioSeleccionado: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_history)

        recyclerView = findViewById(R.id.rvOperadores)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistorialOperadoresAdapter(emptyList())
        recyclerView.adapter = adapter

        searchEditText = findViewById(R.id.searchEditText)
        btnFiltro = findViewById(R.id.btnFiltro)
        tvFiltroAplicado = findViewById(R.id.tvFiltroAplicado)

        cargarOperadores()

        // Exportar
        val btnExportar = findViewById<ImageView>(R.id.btnExportar)
        btnExportar.setOnClickListener {
            val intent = Intent(this, ExportSchedulesActivity::class.java)
            startActivity(intent)
        }

        // Filtro (BottomSheet)
        btnFiltro.setOnClickListener {
            lifecycleScope.launch {
                val carreras = provider.getCareerNames()
                val laboratorios = provider.getLaboratoryName() // <-- AQUÍ obtienes los labs en tiempo real

                showFiltroBottomSheet(
                    this@HistorialOperadoresActivity,
                    carreras,
                    laboratorios,   // <-- Pasa la lista dinámica aquí
                    filtroActual,
                    carreraSeleccionada
                ) { nuevoFiltro, nuevaCarrera, nuevoLab, nuevoDia, nuevoHorario ->
                    filtroActual = nuevoFiltro
                    carreraSeleccionada = nuevaCarrera
                    laboratorioSeleccionado = nuevoLab
                    diaSeleccionado = nuevoDia
                    horarioSeleccionado = nuevoHorario
                    updateSearchUI()
                    searchEditText.text.clear()
                    if (filtroActual == TipoFiltro.CARRERA ||
                        filtroActual == TipoFiltro.LABORATORIO ||
                        filtroActual == TipoFiltro.DIA_HORARIO
                    ) {
                        aplicarFiltro("")
                    }
                }
            }
        }

        // Filtrado en tiempo real
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltro(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Carga operadores de Firestore usando tu Provider (optimizado para modelo OperadorCompleto)
    private fun cargarOperadores() {
        lifecycleScope.launch {
            try {
                operadoresList = provider.getAllOperadores()
                adapter.actualizarLista(operadoresList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Aplica el filtro seleccionado (query es el texto del buscador, solo usado si no es carrera)
    // Dentro de tu activity, si ya tienes la lista con laboratorios y horarios en memoria
    private fun aplicarFiltro(query: String) {
        when (filtroActual) {
            TipoFiltro.CARNET -> {
                val listaFiltrada = operadoresList.filter { it.carnet.contains(query, ignoreCase = true) }
                adapter.actualizarLista(listaFiltrada)
            }
            TipoFiltro.NOMBRE -> {
                val listaFiltrada = operadoresList.filter { it.nombre.contains(query, ignoreCase = true) }
                adapter.actualizarLista(listaFiltrada)
            }
            TipoFiltro.CARRERA -> {
                val listaFiltrada = operadoresList.filter { it.carrera == carreraSeleccionada }
                adapter.actualizarLista(listaFiltrada)
            }
            TipoFiltro.LABORATORIO -> {
                // FILTRO EN MEMORIA (NO EN FIRESTORE)
                val listaLab = operadoresList.filter { it.laboratorios.containsKey(laboratorioSeleccionado) }
                adapter.actualizarLista(listaLab)
            }
            TipoFiltro.DIA_HORARIO -> {
                // FILTRO EN MEMORIA POR DÍA Y HORARIO EN CUALQUIER LABORATORIO
                val listaDiaHorario = operadoresList.filter { op ->
                    op.laboratorios.any { (_, diasMap) ->
                        diasMap[diaSeleccionado]?.contains(horarioSeleccionado) == true
                    }
                }
                adapter.actualizarLista(listaDiaHorario)
            }
        }
    }

    // Actualiza UI según tipo de filtro
    private fun updateSearchUI() {
        when (filtroActual) {
            TipoFiltro.CARNET -> {
                searchEditText.hint = "Buscar por carnet"
                searchEditText.inputType = InputType.TYPE_CLASS_NUMBER
                searchEditText.isEnabled = true
                tvFiltroAplicado.text = "Filtro aplicado: Carnet"
            }
            TipoFiltro.NOMBRE -> {
                searchEditText.hint = "Buscar por nombre"
                searchEditText.inputType = InputType.TYPE_CLASS_TEXT
                searchEditText.isEnabled = true
                tvFiltroAplicado.text = "Filtro aplicado: Nombre"
            }
            TipoFiltro.CARRERA -> {
                searchEditText.hint = "Seleccione una carrera"
                searchEditText.isEnabled = false
                tvFiltroAplicado.text = "Filtro aplicado: Carrera${if (carreraSeleccionada != null) " (${carreraSeleccionada})" else ""}"
            }
            TipoFiltro.LABORATORIO -> {
                searchEditText.hint = "Seleccione un laboratorio"
                searchEditText.isEnabled = false
                tvFiltroAplicado.text = "Filtro aplicado: Laboratorio${if (laboratorioSeleccionado != null) " (${laboratorioSeleccionado})" else ""}"
            }
            TipoFiltro.DIA_HORARIO -> {
                searchEditText.hint = "Seleccione día y horario"
                searchEditText.isEnabled = false
                val info = if (diaSeleccionado != null && horarioSeleccionado != null)
                    " ($diaSeleccionado - $horarioSeleccionado)" else ""
                tvFiltroAplicado.text = "Filtro aplicado: Día y Horario$info"
            }
        }
        tvFiltroAplicado.visibility = View.VISIBLE
        btnFiltro.isEnabled = true
        btnFiltro.alpha = 1f
    }

    // --- BottomSheetDialog para el filtro ---
    enum class TipoFiltro { CARNET, NOMBRE, CARRERA, LABORATORIO, DIA_HORARIO }

    fun showFiltroBottomSheet(
        context: Context,
        carreras: List<String>,
        laboratorios: List<String>, // <--- NUEVO
        filtroActual: TipoFiltro,
        carreraSeleccionada: String?,
        onFiltroSeleccionado: (TipoFiltro, String?, String?, String?, String?) -> Unit
    )
    {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.bottomsheet_filtro_operadores, null)

        // RadioButtons
        val rgFiltro = dialogView.findViewById<RadioGroup>(R.id.rgFiltro)
        val rbCarnet = dialogView.findViewById<RadioButton>(R.id.rbCarnet)
        val rbNombre = dialogView.findViewById<RadioButton>(R.id.rbNombre)
        val rbCarrera = dialogView.findViewById<RadioButton>(R.id.rbCarrera)
        val rbLaboratorio = dialogView.findViewById<RadioButton>(R.id.rbLaboratorio)
        val rbDiaHorario = dialogView.findViewById<RadioButton>(R.id.rbDiaHorario)

        // Selectores Carrera
        val spinnerCarreras = dialogView.findViewById<Spinner>(R.id.spinnerCarreras)
        val tvSeleccionarCarrera = dialogView.findViewById<TextView>(R.id.tvSeleccionarCarrera)

        // Selectores Laboratorio
        val spinnerLaboratorios = dialogView.findViewById<Spinner>(R.id.spinnerLaboratorios)
        val tvSeleccionarLaboratorio = dialogView.findViewById<TextView>(R.id.tvSeleccionarLaboratorio)

        // Selectores Día/Horario
        val spinnerDias = dialogView.findViewById<Spinner>(R.id.spinnerDias)
        val spinnerHorarios = dialogView.findViewById<Spinner>(R.id.spinnerHorarios)
        val tvSeleccionarDiaHorario = dialogView.findViewById<TextView>(R.id.tvSeleccionarDiaHorario)

        // Llena los spinners
        spinnerCarreras.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, carreras)
        val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val horarios = listOf("Mañana", "Tarde", "Noche")
        spinnerLaboratorios.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, laboratorios)
        spinnerDias.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, dias)
        spinnerHorarios.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, horarios)

        // Lógica de visibilidad
        fun updateVisibilities() {
            // Carrera
            val isCarrera = rbCarrera.isChecked
            tvSeleccionarCarrera.visibility = if (isCarrera) View.VISIBLE else View.GONE
            spinnerCarreras.visibility = if (isCarrera) View.VISIBLE else View.GONE

            // Laboratorio
            val isLab = rbLaboratorio.isChecked
            tvSeleccionarLaboratorio.visibility = if (isLab) View.VISIBLE else View.GONE
            spinnerLaboratorios.visibility = if (isLab) View.VISIBLE else View.GONE

            // Día y horario
            val isDiaHorario = rbDiaHorario.isChecked
            tvSeleccionarDiaHorario.visibility = if (isDiaHorario) View.VISIBLE else View.GONE
            spinnerDias.visibility = if (isDiaHorario) View.VISIBLE else View.GONE
            spinnerHorarios.visibility = if (isDiaHorario) View.VISIBLE else View.GONE
        }

        // Aplica visibilidad al cambiar filtro
        rgFiltro.setOnCheckedChangeListener { _, _ -> updateVisibilities() }

        // Preselecciona lo que se está usando
        when (filtroActual) {
            TipoFiltro.CARNET -> rgFiltro.check(R.id.rbCarnet)
            TipoFiltro.NOMBRE -> rgFiltro.check(R.id.rbNombre)
            TipoFiltro.CARRERA -> {
                rgFiltro.check(R.id.rbCarrera)
                carreraSeleccionada?.let {
                    val idx = carreras.indexOf(it)
                    if (idx >= 0) spinnerCarreras.setSelection(idx)
                }
            }
            TipoFiltro.LABORATORIO -> rgFiltro.check(R.id.rbLaboratorio)
            TipoFiltro.DIA_HORARIO -> rgFiltro.check(R.id.rbDiaHorario)
        }
        // Actualiza visibilidad al cargar
        updateVisibilities()

        val btnAplicar = dialogView.findViewById<Button>(R.id.btnAplicarFiltro)
        val dialog = BottomSheetDialog(context)
        dialog.setContentView(dialogView)

        btnAplicar.setOnClickListener {
            val tipo = when (rgFiltro.checkedRadioButtonId) {
                R.id.rbCarnet -> TipoFiltro.CARNET
                R.id.rbNombre -> TipoFiltro.NOMBRE
                R.id.rbCarrera -> TipoFiltro.CARRERA
                R.id.rbLaboratorio -> TipoFiltro.LABORATORIO
                R.id.rbDiaHorario -> TipoFiltro.DIA_HORARIO
                else -> TipoFiltro.NOMBRE
            }
            val carrera = if (tipo == TipoFiltro.CARRERA) spinnerCarreras.selectedItem as String else null
            val lab = if (tipo == TipoFiltro.LABORATORIO) spinnerLaboratorios.selectedItem as String else null
            val dia = if (tipo == TipoFiltro.DIA_HORARIO) spinnerDias.selectedItem as String else null
            val horario = if (tipo == TipoFiltro.DIA_HORARIO) spinnerHorarios.selectedItem as String else null
            dialog.dismiss()
            onFiltroSeleccionado(tipo, carrera, lab, dia, horario)
        }
        dialog.show()
    }

    fun backOperatorHistory(view: View) {
        finish()
    }
}
