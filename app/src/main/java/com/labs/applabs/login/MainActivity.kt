package com.labs.applabs.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.R
import com.labs.applabs.RegisterActivity
import com.labs.applabs.ResetPasswordActivity
import com.labs.applabs.models.ValidadorCampos

class MainActivity : AppCompatActivity() {
    //Inputs y botones
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRecordar: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    //Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    //Validador
    private val validador = object : ValidadorCampos() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias de vista
        etCorreo = findViewById(R.id.et1)
        etPassword = findViewById(R.id.et2)
        btnLogin = findViewById(R.id.btn2)
        btnRegister = findViewById(R.id.btn3)
        cbRecordar = findViewById(R.id.cbRecordar)
        val btnOlvidar = findViewById<Button>(R.id.btn1)
        val ivToggle = findViewById<ImageView>(R.id.iv_toggle)

        // Mostrar/ocultar contraseña
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

        // Cargar credenciales guardadas
        val prefs = getSharedPreferences("Credenciales", MODE_PRIVATE)
        etCorreo.setText(prefs.getString("correo", ""))
        etPassword.setText(prefs.getString("clave", ""))
        cbRecordar.isChecked = prefs.getBoolean("recordar", false)

        // Botón login
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validar correo institucional
            val errorCorreo = validador.validarCorreo(correo)
            if (errorCorreo != null) {
                etCorreo.error = errorCorreo
                return@setOnClickListener
            }

            // Validar contraseña segura
            val errorPass = validador.validarContrasena(password)
            if (errorPass != null) {
                etPassword.error = errorPass
                return@setOnClickListener
            }

            // Autenticación con Firebase
            auth.signInWithEmailAndPassword(correo, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    // Guardar o borrar credenciales según el checkbox
                    if (cbRecordar.isChecked) {
                        prefs.edit().apply {
                            putString("correo", correo)
                            putString("clave", password)
                            putBoolean("recordar", true)
                            apply()
                        }
                    } else {
                        prefs.edit().clear().apply()
                    }

                    verificarRolYRedirigir(uid)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al iniciar sesión: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Ir a registro
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Ir a recuperar contraseña
        btnOlvidar.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    // Verificar rol y redirigir a vista correspondiente
    private fun verificarRolYRedirigir(uid: String) {
        db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val rol = doc.getLong("rol")?.toInt() ?: 3
                when (rol) {
                    1 -> startActivity(Intent(this, com.labs.applabs.administrator.DetailFormActivity::class.java))
                    // 2 -> startActivity(Intent(this, OperadorActivity::class.java)) // pendiente
                    // 3 -> startActivity(Intent(this, EstudianteActivity::class.java)) // pendiente
                }
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener rol", Toast.LENGTH_SHORT).show()
            }
    }
}
