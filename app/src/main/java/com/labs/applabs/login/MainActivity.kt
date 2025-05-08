package com.labs.applabs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.administrator.AdminMenuActivity
import com.labs.applabs.login.RegisterActivity

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✔️ Hacer transparentes barra de estado y barra de navegación
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.et1)
        passwordEditText = findViewById(R.id.et2)
        loginButton = findViewById(R.id.btnLogin)
        registerButton = findViewById(R.id.btn3)

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    fun menuAdmin(view: View) {
        val intent = Intent(this, AdminMenuActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            Toast.makeText(this, "Sesión iniciada automáticamente", Toast.LENGTH_SHORT).show()
        }
    }
}
