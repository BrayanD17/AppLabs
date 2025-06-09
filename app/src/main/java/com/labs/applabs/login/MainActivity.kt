package com.labs.applabs.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.R
import com.labs.applabs.RegisterActivity
import com.labs.applabs.ResetPasswordActivity
import com.labs.applabs.administrator.AdminMenuActivity
import com.labs.applabs.firebase.Provider
import com.labs.applabs.models.ValidadorCampos

class MainActivity : AppCompatActivity() {

    // Inputs y botones
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRecordar: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    // Firebase Auth y lógica Provider
    private val auth = FirebaseAuth.getInstance()
    private val provider = Provider()

    // Validador
    private val validador = object : ValidadorCampos() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hacer transparentes barra de estado y barra de navegación
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        etCorreo = findViewById(R.id.et1)
        etPassword = findViewById(R.id.et2)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btn3)
        cbRecordar = findViewById(R.id.cbRecordar)
        val btnOlvidar = findViewById<Button>(R.id.btn1)
        val ivToggle = findViewById<ImageView>(R.id.iv_toggle)

        // Mostrar u ocultar contraseña
        var isPasswordVisible = false
        ivToggle.setOnClickListener {
            etPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            ivToggle.setImageResource(if (isPasswordVisible) R.drawable.eye_hide else R.drawable.eye_open)
            etPassword.setSelection(etPassword.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        // Cargar credenciales guardadas si las hay
        val prefs = getSharedPreferences("Credenciales", MODE_PRIVATE)
        etCorreo.setText(prefs.getString("correo", ""))
        etPassword.setText(prefs.getString("clave", ""))
        cbRecordar.isChecked = prefs.getBoolean("recordar", false)

        // Login
        btnLogin.setOnClickListener {
            val email = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validaciones
            val errorCorreo = validador.validarCorreo(email)
            if (errorCorreo != null) {
                etCorreo.error = errorCorreo
                return@setOnClickListener
            }

            val errorPass = validador.validarContrasena(password)
            if (errorPass != null) {
                etPassword.error = errorPass
                return@setOnClickListener
            }

            // Login Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        auth.signOut()
                        Toast.makeText(
                            this,
                            "Debes verificar tu correo antes de iniciar sesión.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    val uid = user?.uid ?: return@addOnSuccessListener

                    // Guardar o borrar credenciales
                    if (cbRecordar.isChecked) {
                        prefs.edit().apply {
                            putString("correo", email)
                            putString("clave", password)
                            putBoolean("recordar", true)
                            apply()
                        }
                    } else {
                        prefs.edit().clear().apply()
                    }

                    provider.verificarRol(uid) { rol ->
                        when (rol) {
                            1 -> startActivity(
                                Intent(
                                    this,
                                    com.labs.applabs.administrator.AdminMenuActivity::class.java
                                )
                            )
                            2 -> startActivity(
                                Intent(
                                    this,
                                    com.labs.applabs.student.studentMenuActivity::class.java
                                )
                            )
                            3 -> startActivity(
                                Intent(
                                    this,
                                    com.labs.applabs.operator.MenuOperatorActivity::class.java
                                )
                            )
                        }
                        finish()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error al iniciar sesión: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }
            // Registro
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Recuperar contraseña
        btnOlvidar.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

}
