package com.labs.applabs.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    // Campos del formulario
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var surnamesEditText: EditText
    private lateinit var cardEditText: EditText
    private lateinit var telephoneEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var bankAccountEditText: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()

        // Asociar vistas
        emailEditText = findViewById(R.id.etMail)
        passwordEditText = findViewById(R.id.etPassword)
        nameEditText = findViewById(R.id.etName)
        surnamesEditText = findViewById(R.id.etSurnames)
        cardEditText = findViewById(R.id.etCard)
        telephoneEditText = findViewById(R.id.etTelephone)
        registerButton = findViewById(R.id.btnRegister)
        bankAccountEditText = findViewById(R.id.etBankAccount)


        // Acción del botón "Registrarse"
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val surnames = surnamesEditText.text.toString()
            val card = cardEditText.text.toString()
            val phone = telephoneEditText.text.toString()
            val bankAccount = bankAccountEditText.text.toString()


            // Verificación básica
            //Las funciones para CRUD de los datos debe ir por fuera del fronted en el provider
            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()
                && surnames.isNotEmpty() && card.isNotEmpty() && phone.isNotEmpty()
                && bankAccount.isNotEmpty()
            ) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, it.exception?.message ?: "Error al registrar", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
