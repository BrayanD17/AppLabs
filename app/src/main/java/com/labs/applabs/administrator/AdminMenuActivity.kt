package com.labs.applabs.administrator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.labs.applabs.R
import com.labs.applabs.login.MainActivity
import com.labs.applabs.login.NewPasswordActivity
import com.labs.applabs.operadores.HistorialOperadoresActivity

class AdminMenuActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_menu)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        // Abrir el drawer con el botón hamburguesa
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Cerrar el drawer con el botón dentro del menú
        val btnCerrarDrawer = navView.findViewById<Button>(R.id.btnCerrarDrawer)
        btnCerrarDrawer.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Opciones del menú lateral
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, AdminMenuActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_change_password -> {
                    val intent = Intent(this, NewPasswordActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Conexiones con botones principales
        viewAplicationForm()
        viewHistorialOperadores()
        viewMenuOperator()
    }

    fun callMenuForm(view: View) {
        val intent = Intent(this, AdminMenuFormActivity::class.java)
        startActivity(intent)
    }

    fun viewAplicationForm() {
        val btnViewApplicationForm = findViewById<ImageButton>(R.id.btnAplicationForm)
        btnViewApplicationForm.setOnClickListener {
            val intent = Intent(this, SolicitudesListView::class.java)
            startActivity(intent)
        }
    }

    fun viewHistorialOperadores() {
        val btnOperadores = findViewById<ImageButton>(R.id.btnOperador)
        btnOperadores.setOnClickListener {
            val intent = Intent(this, HistorialOperadoresActivity::class.java)
            startActivity(intent)
        }
    }

    fun viewMenuOperator(){
        val btnMenu = findViewById<ImageButton>(R.id.btnOperador)
        btnMenu.setOnClickListener {
            val intent = Intent(this, AdminMenuOperatorActivity::class.java)
            startActivity(intent)
        }
    }
}
