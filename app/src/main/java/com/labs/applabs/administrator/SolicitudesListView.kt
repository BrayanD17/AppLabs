package com.labs.applabs.administrator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.labs.applabs.administrator.Adapter.SolicitudAdapter
import com.labs.applabs.elements.FiltroDialogFragment
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.Solicitud
import kotlinx.coroutines.launch

class SolicitudesListView : AppCompatActivity() {

    private val provider : Provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SolicitudAdapter
    private lateinit var filters : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes_list_view)

        initViews()
        setupRecyclerView()
        loadData()

        filters = findViewById<ImageView>(R.id.filterIcon)
        filters.setOnClickListener {
            // Crea una instancia del DialogFragment
            val filtroDialog = FiltroDialogFragment()

            // Muestra el diálogo usando el FragmentManager
            filtroDialog.show(
                (this as FragmentActivity).supportFragmentManager,
                "FiltroDialogFragment"
            )
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
                adapter.actualizarLista(solicitudes)
            } catch (e: Exception) {
                showError(e.message ?: "Error desconocido")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }


}