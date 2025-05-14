package com.labs.applabs.student

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.labs.applabs.MainActivity
import com.labs.applabs.R
import com.labs.applabs.administrator.AdminMenuActivity

class studentMenuActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout_Student)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_Student)
        val navView = findViewById<NavigationView>(R.id.nav_view_student)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenuStudent)

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
                R.id.nav_logout -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
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

        val notificationIcon = findViewById<CardView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            showNotificationsFragment()
        }

        // Detectar cambios en el back stack para actualizar
        supportFragmentManager.addOnBackStackChangedListener {
            val isFragmentVisible = supportFragmentManager.backStackEntryCount > 0
            val welcomeText = findViewById<TextView>(R.id.welcomeText)
            val logoImage = findViewById<ImageView>(R.id.logoImage)
            val formCard = findViewById<CardView>(R.id.formCard)
            val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)

            if (isFragmentVisible) {
                welcomeText.visibility = View.GONE
                logoImage.visibility = View.GONE
                formCard.visibility = View.GONE
                fragmentContainer.visibility = View.VISIBLE
            } else {
                welcomeText.visibility = View.VISIBLE
                logoImage.visibility = View.VISIBLE
                formCard.visibility = View.VISIBLE
                fragmentContainer.visibility = View.GONE
            }
        }
    }

    private fun showNotificationsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NotificationsFragment.newInstance())
            .addToBackStack("notifications")
            .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}