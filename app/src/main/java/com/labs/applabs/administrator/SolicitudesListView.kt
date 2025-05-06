package com.labs.applabs.administrator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.Adapter.SolicitudAdapter

class SolicitudesListView : AppCompatActivity() {


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

        val recyclerView = findViewById<RecyclerView>(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val listaTarjetas = listOf(
            Solicitud("Samantha Acuña", "samantha23@estudiantec.cr"),
            Solicitud("Samantha Acuña", "samantha23@estudiantec.cr"),
            Solicitud("Samantha Acuña", "samantha23@estudiantec.cr"),
        )

        recyclerView.adapter = SolicitudAdapter(listaTarjetas)
    }
}