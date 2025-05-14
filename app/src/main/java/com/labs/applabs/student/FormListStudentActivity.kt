package com.labs.applabs.student

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.dialogConfirmDelete
import com.labs.applabs.elements.toastMessage
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
        finishActivity()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initRecyclerView() {
        recyclerViewListForm = findViewById(R.id.recycleViewStudentForm)
        adapter = FormListAdapter(mutableListOf())
        recyclerViewListForm.layoutManager = LinearLayoutManager(this)
        recyclerViewListForm.adapter = adapter

        lifecycleScope.launch {
            val studentOperatorForm = provider.getInfoStudentForm()
            adapter.updateList(studentOperatorForm)
        }

        adapter.setOnItemClickListener { listForm ->
            val intent = Intent(this, DetailsFormStudentActivity::class.java)
            intent.putExtra("formIdStudent", listForm.FormIdStudent)
            startActivity(intent)
        }

        adapter.setOnEditClickListener { listForm ->
            val intent = Intent(this, EditFormStudent::class.java)
            intent.putExtra("formId", listForm.FormIdStudent)
            startActivity(intent)
        }

        adapter.setOnDeleteClickListener { listForm->
            dialogConfirmDelete { provider.deleteFormStudent(listForm.FormIdStudent){success->
                if(success){
                    toastMessage("Formulario eliminado correctamente",ToastType.SUCCESS)
                    adapter.removeItem(listForm)
                }else{
                    toastMessage("Error al eliminar el formulario",ToastType.ERROR)
                }
            } }

        }

    }

    private fun finishActivity(){
        val backView = findViewById<ImageView>(R.id.backViewStudent)
        backView.setOnClickListener {
            val intent = Intent(this, studentMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}