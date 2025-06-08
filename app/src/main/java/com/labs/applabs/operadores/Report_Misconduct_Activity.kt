package com.labs.applabs.operadores

import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider

class Report_Misconduct_Activity : AppCompatActivity() {

    val provider = Provider()
    private lateinit var labora: Spinner
    private lateinit var student: Spinner
    private lateinit var semester: Spinner
    private lateinit var comment: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_misconduct)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}