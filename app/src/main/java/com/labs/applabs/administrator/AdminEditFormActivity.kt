package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.administrator.Adapter.FormOperadorAdapter
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class AdminEditFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_edit_form)

        val provider = Provider()
        val listView = findViewById<ListView>(R.id.listViewFormularios)

        lifecycleScope.launch {
            val formularios = provider.getAllFormOperators()
            val adapter = FormOperadorAdapter(this@AdminEditFormActivity, formularios)
            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, position, _ ->
                val form = formularios[position]

                val intent = Intent(this@AdminEditFormActivity, AdminEditMenuActivity::class.java)
                intent.putExtra("nameForm", form.nameForm.trim())
                intent.putExtra("semester", form.semester.trim())
                intent.putExtra("year", form.year)
                startActivity(intent)
            }


        }


    }
}