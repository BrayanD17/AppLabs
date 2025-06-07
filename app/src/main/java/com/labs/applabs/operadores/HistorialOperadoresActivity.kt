package com.labs.applabs.operadores

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.R
import com.labs.applabs.export.ExportSchedulesActivity
import com.labs.applabs.operadores.Adapter.HistorialOperadoresAdapter
import kotlinx.coroutines.tasks.await

class HistorialOperadoresActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistorialOperadoresAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_operadores)

        recyclerView = findViewById(R.id.rvOperadores)
        adapter = HistorialOperadoresAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        cargarOperadores()

        val btnExportar = findViewById<ImageView>(R.id.btnExportar)
        btnExportar.setOnClickListener {
            val intent = Intent(this, ExportSchedulesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarOperadores() {
        lifecycleScope.launchWhenStarted {
            try {
                val snapshot = db.collection("historialOperadores").get().await()
                val lista = snapshot.toObjects(OperadorHistorial::class.java)
                adapter.actualizarLista(lista)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
