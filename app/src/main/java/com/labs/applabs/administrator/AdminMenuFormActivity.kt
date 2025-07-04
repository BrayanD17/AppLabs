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
import com.labs.applabs.login.MainActivity
import com.labs.applabs.R
import com.labs.applabs.login.NewPasswordActivity

class AdminMenuFormActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_menu_form)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu2)

        // Abrir el drawer con el botón hamburguesa
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Cerrar el drawer con el botón dentro del menú
        val btnCerrarDrawer = navView.findViewById<Button>(R.id.btnCerrarDrawer)
        btnCerrarDrawer.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Opciones del menú
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
    }
    fun callAdminAddForm(view:View){
        val intent = Intent(this, AdminAddFormActivity::class.java)
        startActivity(intent)
    }

    fun callAdminEditForm(view:View){
        val intent = Intent(this, AdminEditFormActivity::class.java)
        startActivity(intent)
    }

}