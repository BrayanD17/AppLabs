package com.labs.applabs.student

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
import com.labs.applabs.student.Adapter.FormListAdapter
import kotlinx.coroutines.launch

class FormListStudentActivity : AppCompatActivity(){
    private val provider : Provider = Provider()
    private lateinit var recyclerViewListForm: RecyclerView
    private lateinit var adapter: FormListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_list_student)
        initRecyclerView()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initRecyclerView() {
        recyclerViewListForm = findViewById(R.id.recycleViewStudentForm)
        adapter = FormListAdapter(emptyList())
        recyclerViewListForm.layoutManager = LinearLayoutManager(this)
        recyclerViewListForm.adapter = adapter

        lifecycleScope.launch {
            val studentOperatorForm = provider.getInfoStudentForm("gfTos90dNJeX8kkffqIo")
            adapter.actualizarLista(studentOperatorForm)
        }

        adapter.setOnItemClickListener { listForm ->
            Toast.makeText(this, "${listForm.FormName}", Toast.LENGTH_SHORT).show()
        }
    }

}