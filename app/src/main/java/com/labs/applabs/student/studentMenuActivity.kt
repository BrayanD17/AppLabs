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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.labs.applabs.login.MainActivity
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.firebase.Provider
import com.labs.applabs.login.NewPasswordActivity
import kotlinx.coroutines.launch

class studentMenuActivity : AppCompatActivity() {
    private val provider = Provider()
    private lateinit var recyclerView: RecyclerView

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

        // FROM MENU (NAVAGATION BAR) STUDENT
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_Student)
        val navView = findViewById<NavigationView>(R.id.nav_view_student)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenuStudent)

        // User Information in Header
        val headerView : View = navView.getHeaderView(0)
        val nameUser : TextView = headerView.findViewById(R.id.tvNombreStudent)
        val rolUser : TextView = headerView.findViewById(R.id.tvRolUsuarioStudent)

        lifecycleScope.launch {
            val user = provider.getUserInformation()
            if (user != null) {
                // Actualiza la UI con los datos
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
                R.id.nav_home_student -> {
                    val intent = Intent(this, this::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_Forms_student -> {
                    val intent = Intent(this, FormListStudentActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_change_password -> {
                    val intent = Intent(this, NewPasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_logout_student -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        loadActiveForm()

        val notificationIcon = findViewById<CardView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            showNotificationsFragment()
        }

    //MENU STUDENT FROM MESSAGE (VISIBILITY OR INVISIBILITY)
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

    //SHOW FRAGMENT FROM MESSAGE
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

    private fun loadActiveForm() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val forms = provider.getActiveForms()

            if (forms.isNotEmpty()) {
                val adapter = CardActiveFormAdapter(forms)
                recyclerView.adapter = adapter

                adapter.setOnItemClickListener { form->
                    val intent = Intent(this@studentMenuActivity, FormActivity::class.java)
                    intent.putExtra("formIdFormActive", form.operatorIdForm)
                    intent.putExtra("formName", form.nameActiveForm)
                    intent.putExtra("semesterForm", form.semesterActive)
                    //Toast.makeText(this@studentMenuActivity, form.operatorIdForm, Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }

            } else {
                recyclerView.visibility = View.GONE
                findViewById<TextView>(R.id.formEmptyMessage)?.apply {
                    text = "No hay formularios activos disponibles."
                    visibility = View.VISIBLE
                }
            }
        }


    }

}