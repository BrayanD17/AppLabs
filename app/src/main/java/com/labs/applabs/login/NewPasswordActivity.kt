package com.labs.applabs.login

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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.R
import com.labs.applabs.models.ValidadorCampos

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
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
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etnewpassword)
        etConfirmPassword = findViewById(R.id.etconfirmpassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        llSuccessMessage = findViewById(R.id.llSuccessMessage)

        // Botón para cambiar contraseña
        btnChangePassword.setOnClickListener {
            val current = etCurrentPassword.text.toString().trim()
            val nueva = etNewPassword.text.toString().trim()
            val confirmar = etConfirmPassword.text.toString().trim()

            // Validar contraseña actual
            if (current.isEmpty()) {
                etCurrentPassword.error = "Ingrese su contraseña actual"
                return@setOnClickListener
            }

            // Validar nueva contraseña
            val errorPass = validador.validarContrasena(nueva)
            if (errorPass != null) {
                etNewPassword.error = errorPass
                return@setOnClickListener
            }

            // Verificar coincidencia con confirmar
            if (nueva != confirmar) {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            // Obtener usuario actual y correo
            val user = auth.currentUser
            val email = user?.email

            // Reautenticar antes de cambiar contraseña
            if (user != null && email != null) {
                val credential = EmailAuthProvider.getCredential(email, current)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        // Si se reautenticó correctamente, actualizar contraseña
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
                                Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener {
                        etCurrentPassword.error = "Contraseña actual incorrecta"
                    }
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
