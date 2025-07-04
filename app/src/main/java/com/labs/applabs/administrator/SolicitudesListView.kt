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
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.Adapter.SolicitudAdapter
import com.labs.applabs.elements.FiltroDialogFragment
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
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

        recyclerView = findViewById(R.id.recycleView)
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

        onBackPressedDispatcher.addCallback(this) {
            finish()
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
        finishActivitySolicitudes()
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
                toastMessage("Error al obtener los datos", ToastType.ERROR)
            }
        }
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
        Log.d("FiltroData", "Datos del filtro: $filterData")

        val filtradas = listaCompletaSolicitudes.filter { solicitud ->
            val carreraOk = filterData.degree.isNullOrBlank() || solicitud.carrera.trim().equals(filterData.degree.trim(), ignoreCase = true)
            val semestreOk = filterData.semester.isNullOrBlank() || solicitud.numeroSemestreOperador.trim() == filterData.semester.trim()
            val carnetOk = filterData.cardStudent.isNullOrBlank() || solicitud.carnet.trim().contains(filterData.cardStudent.trim(), ignoreCase = true)
            val estadoOk = filterData.status.isNullOrBlank() || solicitud.estado.trim().equals(filterData.status.trim(), ignoreCase = true)

            Log.d("FiltroPaso", """
            ---
            Solicitud: ${solicitud.nombre}
            Comparando:
            carrera: '${solicitud.carrera}' vs '${filterData.degree}' = $carreraOk
            semestre: '${solicitud.numeroSemestreOperador}' vs '${filterData.semester}' = $semestreOk
            carnet: '${solicitud.carnet}' vs '${filterData.cardStudent}' = $carnetOk
            estado: '${solicitud.estado}' vs '${filterData.status}' = $estadoOk
        """.trimIndent())

            carreraOk && semestreOk && carnetOk && estadoOk
        }

        Log.d("Filtro", "Total filtradas: ${filtradas.size}")
        listaFiltradaSolicitudes = filtradas
        adapter.actualizarLista(listaFiltradaSolicitudes)
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