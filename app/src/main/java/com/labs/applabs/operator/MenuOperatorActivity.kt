package com.labs.applabs.operator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import com.labs.applabs.login.MainActivity
import com.labs.applabs.login.NewPasswordActivity
import com.labs.applabs.student.FormListStudentActivity
import kotlinx.coroutines.launch

class MenuOperatorActivity : AppCompatActivity() {
    private val provider = Provider()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_operator)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_Operator)
        val navView = findViewById<NavigationView>(R.id.nav_view_operator)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenuOperator)

        // User Information in Header
        val headerView : View = navView.getHeaderView(0)
        val nameUser : TextView = headerView.findViewById(R.id.tvNombreStudent)
        val rolUser : TextView = headerView.findViewById(R.id.tvRolUsuarioStudent)

        lifecycleScope.launch {
            val user = provider.getUserInformation()
            if (user != null) {
                nameUser.text = user.nameUser
                rolUser.text = user.rolUser
            }
        }

        // Abrir el drawer con el botón hamburguesa
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Close drawer with the button inside
        val btnCerrarDrawer = navView.findViewById<Button>(R.id.btnCerrarDrawer)
        btnCerrarDrawer.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Opciones del menú
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home_operator -> {
                    val intent = Intent(this, this::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_change_password -> {
                    val intent = Intent(this, NewPasswordActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout_operator -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

}