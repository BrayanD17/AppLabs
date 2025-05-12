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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_solicitudes_list_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recycleView)
        adapter = SolicitudAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Cargar datos
        lifecycleScope.launch {
            val solicitudes = provider.getSolicitudes()
            adapter.actualizarLista(solicitudes) // actualiza la lista
        }

        // Opcional: clics en cada solicitud
        adapter.setOnItemClickListener { solicitud ->
            // Aquí puedes abrir un modal o actividad con más detalles
            // Ejemplo: mostrar un Toast
            Toast.makeText(this, "${solicitud.nombre} - ${solicitud.correo}", Toast.LENGTH_SHORT).show()
        }

    }
}