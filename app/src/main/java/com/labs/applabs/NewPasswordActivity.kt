package com.labs.applabs

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.models.ValidadorCampos
import com.labs.applabs.login.MainActivity

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var llSuccessMessage: LinearLayout

    private val validador = object : ValidadorCampos() {}
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        // Referencias del layout
        etNewPassword = findViewById(R.id.etnewpassword)
        etConfirmPassword = findViewById(R.id.etconfirmpassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        llSuccessMessage = findViewById(R.id.llSuccessMessage)

        // Botón para cambiar contraseña
        btnChangePassword.setOnClickListener {
            val nueva = etNewPassword.text.toString().trim()
            val confirmar = etConfirmPassword.text.toString().trim()

            // Validar contraseña
            val errorPass = validador.validarContrasena(nueva)
            if (errorPass != null) {
                etNewPassword.error = errorPass
                return@setOnClickListener
            }

            // Verificar coincidencia
            if (nueva != confirmar) {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            // Actualizar contraseña en Firebase (solo si está autenticado)
            val user = auth.currentUser
            if (user != null) {
                user.updatePassword(nueva)
                    .addOnSuccessListener {
                        // Mostrar mensaje visual
                        llSuccessMessage.visibility = View.VISIBLE

                        // Ocultar mensaje después de 3 segundos y redirigir
                        Handler(Looper.getMainLooper()).postDelayed({
                            llSuccessMessage.visibility = View.GONE
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }, 3000)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
