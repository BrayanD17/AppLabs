package com.labs.applabs.administrator.operator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R
import com.labs.applabs.administrator.AdminSetTimeOperatorActivity

class AdminMenuOperatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_menu_operator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewActiveOperator()
    }

    fun viewActiveOperator(){
        val btnOperatorActive = findViewById<ImageButton>(R.id.btnOperatorActive)
        btnOperatorActive.setOnClickListener {
            val intent = Intent(this, HistorialOperadoresActivity::class.java)
            startActivity(intent)
        }
    }

    fun callToSetTimeOperator(view: View) {
        val intent = Intent(this, AdminSetTimeOperatorActivity::class.java)
        startActivity(intent)
    }

    fun actionBtnMenuBack(view: android.view.View) {
        finish()
    }
}