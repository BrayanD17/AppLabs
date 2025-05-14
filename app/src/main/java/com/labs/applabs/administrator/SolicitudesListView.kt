package com.labs.applabs.administrator

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.Adapter.SolicitudAdapter
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class SolicitudesListView : AppCompatActivity() {

    private val provider : Provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SolicitudAdapter

    @SuppressLint("MissingInflatedId")
    // Versión simplificada pero mejorada de tu código original
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes_list_view)

        recyclerView = findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SolicitudAdapter(emptyList())
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { solicitud ->
            
        }

        lifecycleScope.launch {
            try {
                val solicitudes = provider.getSolicitudes()
                adapter.actualizarLista(solicitudes) // Usa submitList en lugar de actualizarLista
            } catch (e: Exception) {
                Toast.makeText(this@SolicitudesListView,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}