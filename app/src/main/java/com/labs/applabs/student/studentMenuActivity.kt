package com.labs.applabs.student

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class studentMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_menu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
