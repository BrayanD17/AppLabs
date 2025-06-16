package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.Adapter.VisitAdapter
import com.labs.applabs.elements.FiltroDialogFragment
import com.labs.applabs.elements.FiltroDialogVisits
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.ReportVisit
import com.labs.applabs.firebase.Solicitud
import com.labs.applabs.firebase.filterDataVisit
import kotlinx.coroutines.launch

class VisitsListActivity : AppCompatActivity(), FiltroDialogVisits.FilterListener {

    var provider  = Provider()
    private lateinit var recycleViewVisits: RecyclerView
    private lateinit var adapter: VisitAdapter
    private lateinit var filters : ImageView
    private var listaCompletaSolicitudes: List<ReportVisit> = emptyList()
    private var listaFiltradaSolicitudes: List<ReportVisit> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_visits_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recycleViewVisits = findViewById(R.id.recycleViewVisits)
        initRecyclerView()

        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        filters = findViewById(R.id.filterIcon)
        filters.setOnClickListener {
            // Crea una instancia del DialogFragment
            val filtroDialog = FiltroDialogVisits()
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


    private fun initRecyclerView() {

        lifecycleScope.launch {
            var visitsIdList = provider.getVisitReport()
            listaCompletaSolicitudes = visitsIdList
            adapter = VisitAdapter(visitsIdList)
            recycleViewVisits.layoutManager = LinearLayoutManager(this@VisitsListActivity)
            recycleViewVisits.adapter = adapter
        }

    }
    
    private fun finishActivitySolicitudes(){
        val backView = findViewById<ImageView>(R.id.backViewVisitList)
        backView.setOnClickListener {
            val intent = Intent(this, AdminVisitiMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun filterSearch(text: String){
        listaFiltradaSolicitudes = if (text.isEmpty()){
            listaCompletaSolicitudes
        }else{
            listaCompletaSolicitudes.filter { visit ->
                visit.student.contains(text, ignoreCase = true)
            }
        }
        adapter.updateData(listaFiltradaSolicitudes)
    }

    override fun onFilterApply(filterData: filterDataVisit) {
        val filtradas = listaCompletaSolicitudes.filter {
            (filterData.cardStudent.isNullOrEmpty() || it.cardStudent == filterData.cardStudent) ||
                    (filterData.laboratory.isNullOrEmpty() || it.laboratory == filterData.laboratory) ||
                    (filterData.date.isNullOrEmpty() || it.date.contains(filterData.date, ignoreCase = true))
        }
        adapter.updateData(filtradas)
    }

    override fun onFilterCancel() {
        adapter.updateData(listaCompletaSolicitudes)
    }
}