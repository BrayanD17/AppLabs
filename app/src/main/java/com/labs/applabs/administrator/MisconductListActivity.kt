package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.Adapter.MisconductAdapter
import com.labs.applabs.elements.FiltroDialogFragment
import com.labs.applabs.elements.FiltroDialogMisconduct
import com.labs.applabs.firebase.FilterDataMisconduct
import com.labs.applabs.firebase.MisconductStudent
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.Solicitud
import kotlinx.coroutines.launch

class MisconductListActivity : AppCompatActivity(), FiltroDialogMisconduct.FilterListener {

    var provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MisconductAdapter
    private lateinit var filters : ImageView
    private var listaCompletaSolicitudes: List<MisconductStudent> = emptyList()
    private var listaFiltradaSolicitudes: List<MisconductStudent> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_misconduct_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.recycleViewMisconduct)
        initRecyclerView()
        finishActivitySolicitudes()

        filters = findViewById(R.id.filterIcon)
        filters.setOnClickListener {
            // Crea una instancia del DialogFragment
            val filtroDialog = FiltroDialogMisconduct()
            filtroDialog.show(
                (this as FragmentActivity).supportFragmentManager,
                "filtroDialog"
            )
        }

        val etSearch = findViewById<EditText>(R.id.searchEditText)
        // Filtrar mientras se escribe (puedes usar un TextWatcher)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // Filtrar al presionar Enter en el teclado
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterSearch(etSearch.text.toString())
                true
            } else {
                false
            }
        }
    }

    fun initRecyclerView(){
        lifecycleScope.launch {
            try {
                recyclerView.layoutManager = LinearLayoutManager(this@MisconductListActivity)
                var lista  = provider.getMisconductStudents()
                listaCompletaSolicitudes = lista
                adapter = MisconductAdapter(listaCompletaSolicitudes)
                recyclerView.adapter = adapter
                adapter.updateList(lista)
            } catch (e: Exception) {
                // Manejo del error, por ejemplo:
                Toast.makeText(this@MisconductListActivity, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                Log.e("MisconductListActivity", "Error al obtener estudiantes: ${e.message}")
            }
        }
    }

    override fun onFilterApply(filterData: FilterDataMisconduct) {
        Log.d("FiltroMisconduct", "Datos del filtro: $filterData")

        val filtradas = listaCompletaSolicitudes.filter { falta ->
            val carnetOk = filterData.carnet.isNullOrBlank() || falta.cardStudent?.trim() == filterData.carnet.trim()
            val semestreOk = filterData.semestres.isNullOrBlank() || falta.semester?.trim().equals(filterData.semestres.trim(), ignoreCase = true)
            val laboratorioOk = filterData.laboratory.isNullOrBlank() || falta.laboratory?.trim().equals(filterData.laboratory.trim(), ignoreCase = true)

            Log.d("FiltroPaso", """
            ---
            Falta: ${falta.cardStudent}
            Comparando:
            carnet: '${falta.cardStudent}' vs '${filterData.carnet}' = $carnetOk
            semestre: '${falta.semester}' vs '${filterData.semestres}' = $semestreOk
            laboratorio: '${falta.laboratory}' vs '${filterData.laboratory}' = $laboratorioOk
        """.trimIndent())

            carnetOk && semestreOk && laboratorioOk
        }

        Log.d("FiltroMisconduct", "Total faltas filtradas: ${filtradas.size}")
        adapter.updateList(filtradas)
    }

    private fun finishActivitySolicitudes(){
        val backView = findViewById<ImageView>(R.id.backViewMisconduct)
        backView.setOnClickListener {
            val intent = Intent(this, AdminVisitiMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onFilterCancel() {
        adapter.updateList(listaCompletaSolicitudes)
    }

    private fun filterSearch(text: String){
        listaFiltradaSolicitudes = if (text.isEmpty()){
            listaCompletaSolicitudes
        }else{
            listaCompletaSolicitudes.filter { solicitud ->
                solicitud.student.contains(text, ignoreCase = true)
            }
        }
        adapter.updateList(listaFiltradaSolicitudes)
    }
}