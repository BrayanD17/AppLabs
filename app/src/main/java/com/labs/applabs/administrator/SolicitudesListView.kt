package com.labs.applabs.administrator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.Adapter.SolicitudAdapter
import com.labs.applabs.elements.FiltroDialogFragment
import com.labs.applabs.firebase.FilterData
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.Solicitud
import kotlinx.coroutines.launch

class SolicitudesListView : AppCompatActivity(), FiltroDialogFragment.FilterListener {

    private val provider : Provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SolicitudAdapter
    private lateinit var filters : ImageView
    private var listaCompletaSolicitudes: List<Solicitud> = emptyList()
    private var listaFiltradaSolicitudes: List<Solicitud> = emptyList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes_list_view)

        initViews()
        setupRecyclerView()
        loadData()

        filters = findViewById(R.id.filterIcon)
        filters.setOnClickListener {
            // Crea una instancia del DialogFragment
            val filtroDialog = FiltroDialogFragment()
            // Muestra el diálogo usando el FragmentManager
            filtroDialog.show(
                (this as FragmentActivity).supportFragmentManager,
                "FiltroDialogFragment"
            )
        }

        /// Configurar EditText para búsqueda
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

    private fun initViews() {
        recyclerView = findViewById(R.id.recycleView)
    }

    private fun setupRecyclerView() {
        adapter = SolicitudAdapter(emptyList()).apply {
            setOnItemClickListener { solicitud ->
                val intent = Intent(this@SolicitudesListView, DetailFormActivity::class.java)
                intent.putExtra("formId", solicitud.idFormStudent)
                startActivity(intent)
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SolicitudesListView)
            adapter = this@SolicitudesListView.adapter
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val solicitudes = provider.getSolicitudes()
                listaCompletaSolicitudes = solicitudes
                adapter.actualizarLista(solicitudes)
            } catch (e: Exception) {
                showError(e.message ?: "Error desconocido")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    private fun finishActivitySolicitudes(){
        val backView = findViewById<ImageView>(R.id.backViewSolicitudesList)
        backView.setOnClickListener {
            val intent = Intent(this, AdminMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onFilterApply(filterData: FilterData) {
        val filtradas = listaCompletaSolicitudes.filter {
            (filterData.carrera.isNullOrEmpty() || it.carrera == filterData.carrera) &&
                    (filterData.semestres.isNullOrEmpty() || it.numeroSemestreOperador == filterData.semestres) &&
                    (filterData.nombre.isNullOrEmpty() || it.nombre.contains(filterData.nombre, ignoreCase = true)) &&
                    (filterData.carnet.isNullOrEmpty() || it.carnet.contains(filterData.carnet, ignoreCase = true)) &&
                    (filterData.estado.isNullOrEmpty() || it.estado == filterData.estado)
        }
        adapter.actualizarLista(filtradas)
    }

    override fun onFilterCancel() {
        adapter.actualizarLista(listaCompletaSolicitudes)
    }

    private fun filterSearch(text: String){
        listaFiltradaSolicitudes = if (text.isEmpty()){
            listaCompletaSolicitudes
        }else{
            listaCompletaSolicitudes.filter { solicitud ->
                solicitud.nombre.contains(text, ignoreCase = true)
            }
        }
        adapter.actualizarLista(listaFiltradaSolicitudes)
    }


}