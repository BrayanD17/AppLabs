package com.labs.applabs.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.R
import com.labs.applabs.R.id.iv_toggle2
import com.labs.applabs.models.ValidadorCampos

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var llSuccessMessage: LinearLayout

    private lateinit var ivToggleNew: ImageView
    private lateinit var ivToggleConfirm: ImageView

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

        // Mostrar siempre la contraseña actual en texto visible
        etCurrentPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        ivToggleNew = findViewById(iv_toggle2)
        ivToggleConfirm = findViewById(R.id.iv_toggle4)

        var isNewVisible = false
        var isConfirmVisible = false

        ivToggleNew.setOnClickListener {
            isNewVisible = !isNewVisible
            etNewPassword.inputType = if (isNewVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etNewPassword.setSelection(etNewPassword.text.length)
            ivToggleNew.setImageResource(if (isNewVisible) R.drawable.eye_open else R.drawable.eye_hide)
        }

        ivToggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            etConfirmPassword.inputType = if (isConfirmVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
            ivToggleConfirm.setImageResource(if (isConfirmVisible) R.drawable.eye_open else R.drawable.eye_hide)
        }

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

            if (user != null && email != null) {
                val credential = EmailAuthProvider.getCredential(email, current)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(nueva)
                            .addOnSuccessListener {
                                llSuccessMessage.visibility = View.VISIBLE
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
