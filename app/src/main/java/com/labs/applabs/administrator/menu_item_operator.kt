package com.labs.applabs.administrator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R
import com.labs.applabs.operadores.HistorialOperadoresActivity

class menu_item_operator : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_item_operator)

        //Call action btnICon
        viewActiveOperator()
    }

    fun viewActiveOperator(){
        val btnOperatorActive = findViewById<ImageButton>(R.id.btnOperatorActive)
        btnOperatorActive.setOnClickListener {
            val intent = Intent(this, HistorialOperadoresActivity::class.java)
            startActivity(intent)
        }
    }

    fun actionBtnMenuBack(View: View){
        finish()
    }
}