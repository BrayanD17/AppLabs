package com.labs.applabs

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.labs.applabs.firebase.Provider
import com.labs.applabs.login.MainActivity
import com.labs.applabs.models.Usuario
import com.labs.applabs.models.ValidadorCampos

class RegisterActivity2 : AppCompatActivity() {

    private lateinit var etMail: EditText
    private lateinit var etCard: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    private var nombre = ""
    private var apellidos = ""
    private var telefono = ""
    private var cuenta = ""

    private val validador = object : ValidadorCampos() {}
    private val provider = Provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register2)

        // Referencias de los inputs
        etMail = findViewById(R.id.etMail)
        etCard = findViewById(R.id.etCard)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etconfirmpassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Recuperar datos del paso anterior
        nombre = intent.getStringExtra("nombre") ?: ""
        apellidos = intent.getStringExtra("apellidos") ?: ""
        telefono = intent.getStringExtra("telefono") ?: ""
        cuenta = intent.getStringExtra("cuenta") ?: ""

        btnRegister.setOnClickListener {
            val correo = etMail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()
            val carnet = etCard.text.toString().trim()

            // Validaciones
            val errorCorreo = validador.validarCorreo(correo)
            if (errorCorreo != null) {
                etMail.error = errorCorreo
                return@setOnClickListener
            }

            val errorPassword = validador.validarContrasena(password)
            if (errorPassword != null) {
                etPassword.error = errorPassword
                return@setOnClickListener
            }

            if (password != confirm) {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            val errorCarnet = validador.validarCarnet(carnet)
            if (errorCarnet != null) {
                etCard.error = errorCarnet
                return@setOnClickListener
            }

            // Crear el objeto Usuario
            val nuevoUsuario = Usuario(
                uid = "",
                nombre = nombre,
                apellidos = apellidos,
                correo = correo,
                telefono = telefono,
                cuentaBancaria = cuenta,
                carnet = carnet,
                rol = null // sin rol, lo asigna el admin luego
            )

            //Usar Provider en lugar de FirebaseUsuarioService
            provider.registrarUsuario(
                context = this,
                correo = correo,
                password = password,
                usuario = nuevoUsuario,
                onSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { mensaje ->
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
