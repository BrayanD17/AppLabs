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

    private val db = FirebaseFirestore.getInstance()
    private var filtroActual = TipoFiltro.NOMBRE
    private var carreraSeleccionada: String? = null
    private var operadoresList: List<OperadorCompleto> = emptyList()
    private var provider = Provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_operadores)

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
                showFiltroBottomSheet(
                    this@HistorialOperadoresActivity,
                    carreras,
                    filtroActual,
                    carreraSeleccionada
                ) { nuevoFiltro, nuevaCarrera ->
                    filtroActual = nuevoFiltro
                    carreraSeleccionada = nuevaCarrera
                    updateSearchUI()
                    searchEditText.text.clear()
                    // Si es por carrera, aplica el filtro de una vez
                    if (filtroActual == TipoFiltro.CARRERA) {
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
    private fun aplicarFiltro(query: String) {
        val listaFiltrada = when (filtroActual) {
            TipoFiltro.CARNET -> operadoresList.filter { it.carnet.contains(query, ignoreCase = true) }
            TipoFiltro.NOMBRE -> operadoresList.filter { it.nombre.contains(query, ignoreCase = true) }
            TipoFiltro.CARRERA -> operadoresList.filter { it.carrera == carreraSeleccionada }
        }
        adapter.actualizarLista(listaFiltrada)
    }

    // Actualiza UI según tipo de filtro
    private fun updateSearchUI() {
        when (filtroActual) {
            TipoFiltro.CARNET -> {
                searchEditText.hint = "Buscar por carnet"
                searchEditText.inputType = InputType.TYPE_CLASS_NUMBER
                searchEditText.isEnabled = true  // HABILITADO
                tvFiltroAplicado.text = "Filtro aplicado: Carnet"
            }
            TipoFiltro.NOMBRE -> {
                searchEditText.hint = "Buscar por nombre"
                searchEditText.inputType = InputType.TYPE_CLASS_TEXT
                searchEditText.isEnabled = true  // HABILITADO
                tvFiltroAplicado.text = "Filtro aplicado: Nombre"
            }
            TipoFiltro.CARRERA -> {
                searchEditText.hint = "Seleccione una carrera"
                searchEditText.isEnabled = false // DESHABILITADO SOLO PARA ESCRITURA
                tvFiltroAplicado.text = "Filtro aplicado: Carrera${if (carreraSeleccionada != null) " (${carreraSeleccionada})" else ""}"
            }
        }
        tvFiltroAplicado.visibility = View.VISIBLE

        // btnFiltro debe estar siempre habilitado
        btnFiltro.isEnabled = true
        btnFiltro.alpha = 1f // Para que nunca se vea "apagado"
    }


    // --- BottomSheetDialog para el filtro ---
    enum class TipoFiltro { CARNET, NOMBRE, CARRERA }

    fun showFiltroBottomSheet(
        context: Context,
        carreras: List<String>,
        filtroActual: TipoFiltro,
        carreraSeleccionada: String?,
        onFiltroSeleccionado: (TipoFiltro, String?) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.bottomsheet_filtro_operadores, null)
        val rgFiltro = dialogView.findViewById<RadioGroup>(R.id.rgFiltro)
        val rbCarnet = dialogView.findViewById<RadioButton>(R.id.rbCarnet)
        val rbNombre = dialogView.findViewById<RadioButton>(R.id.rbNombre)
        val rbCarrera = dialogView.findViewById<RadioButton>(R.id.rbCarrera)
        val spinnerCarreras = dialogView.findViewById<Spinner>(R.id.spinnerCarreras)
        val tvSeleccionarCarrera = dialogView.findViewById<TextView>(R.id.tvSeleccionarCarrera)
        val btnAplicar = dialogView.findViewById<Button>(R.id.btnAplicarFiltro)

        // Spinner carreras
        val adapterSpinner = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, carreras)
        spinnerCarreras.adapter = adapterSpinner

        // Mostrar spinner y label solo si es carrera
        fun updateCarreraVisibility(visible: Boolean) {
            spinnerCarreras.visibility = if (visible) View.VISIBLE else View.GONE
            tvSeleccionarCarrera.visibility = if (visible) View.VISIBLE else View.GONE
        }

        rgFiltro.setOnCheckedChangeListener { _, checkedId ->
            updateCarreraVisibility(checkedId == R.id.rbCarrera)
        }

        // Preselección
        when (filtroActual) {
            TipoFiltro.CARNET -> rgFiltro.check(R.id.rbCarnet)
            TipoFiltro.NOMBRE -> rgFiltro.check(R.id.rbNombre)
            TipoFiltro.CARRERA -> {
                rgFiltro.check(R.id.rbCarrera)
                updateCarreraVisibility(true)
                carreraSeleccionada?.let {
                    spinnerCarreras.setSelection(carreras.indexOf(it))
                }
            }
        }

        val dialog = BottomSheetDialog(context)
        dialog.setContentView(dialogView)

        btnAplicar.setOnClickListener {
            val tipo = when (rgFiltro.checkedRadioButtonId) {
                R.id.rbCarnet -> TipoFiltro.CARNET
                R.id.rbNombre -> TipoFiltro.NOMBRE
                R.id.rbCarrera -> TipoFiltro.CARRERA
                else -> TipoFiltro.NOMBRE
            }
            val carrera = if (tipo == TipoFiltro.CARRERA) {
                spinnerCarreras.selectedItem as String
            } else null
            dialog.dismiss()
            onFiltroSeleccionado(tipo, carrera)
        }

        dialog.show()
    }
}
