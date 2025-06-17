package com.labs.applabs.administrator.operator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.operator.Adapter.AdapterShowAllOperator
import com.labs.applabs.elements.FiltroDialogFragment
import com.labs.applabs.firebase.OperatorItem
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.Solicitud
import kotlinx.coroutines.launch

class ShowAllOperatorSemester : AppCompatActivity() {

    private val provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterShowAllOperator
    private lateinit var filters : ImageView
    private var listaCompletaSolicitudes: List<OperatorItem> = emptyList()
    private var listaFiltradaSolicitudes: List<OperatorItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_all_operator_semester)

        recyclerView = findViewById(R.id.recycleViewShowAllOperator)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterShowAllOperator(emptyList())

        //Event click
        adapter.setOnItemClickListener { userId ->
            val intent = Intent(this, viewInformationOperator::class.java)
            //Send type activity and userId
            intent.putExtra("type","show")
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        val title = findViewById<TextView>(R.id.titleTextShowAllOperator)

        //Get data from activity HistoryOperatorSemesters
        val userIdList = intent.getStringArrayListExtra("listUsers") ?: emptyList<String>()
        val semester = intent.getStringExtra("semester") ?: ""
        title.text = semester

        //Load data for each user
        lifecycleScope.launch {
            val users = mutableListOf<OperatorItem>()
            for (userId in userIdList) {
                try {
                    val userInfo = provider.getUserInfo(userId)
                    if (userInfo != null) {
                        users.add(OperatorItem(userId, userInfo))  //Load userId for each one
                    }
                } catch (e: Exception) {
                    Log.e("ShowAllOperatorSemester", "Error con $userId: ${e.message}")
                }
            }
            listaCompletaSolicitudes = users
            adapter.update(users)
        }

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

    private fun filterSearch(text: String){
        listaFiltradaSolicitudes = if (text.isEmpty()){
            listaCompletaSolicitudes
        }else{
            listaCompletaSolicitudes.filter { solicitud ->
                solicitud.data.studentInfo.studentName.contains(text, ignoreCase = true)
            }
        }
        adapter.update(listaFiltradaSolicitudes)
    }

    fun backViewShowAllOperator(view: android.view.View) {finish()}
}