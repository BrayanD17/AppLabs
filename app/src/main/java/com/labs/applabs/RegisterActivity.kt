package com.labs.applabs.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.R
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.MainActivity


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
        val firestore = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.etMail)
        passwordEditText = findViewById(R.id.etPassword)
        nameEditText = findViewById(R.id.etName)
        surnamesEditText = findViewById(R.id.etSurnames)
        cardEditText = findViewById(R.id.etCard)
        telephoneEditText = findViewById(R.id.etTelephone)
        bankAccountEditText = findViewById(R.id.etBankAccount)
        registerButton = findViewById(R.id.btnRegister)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val surnames = surnamesEditText.text.toString()
            val card = cardEditText.text.toString()
            val phone = telephoneEditText.text.toString()
            val bankAccount = bankAccountEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()
                && surnames.isNotEmpty() && card.isNotEmpty()
                && phone.isNotEmpty() && bankAccount.isNotEmpty()
            ) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId =
                                firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener

                            // Crear un mapa con los datos
                            val userMap = hashMapOf(
                                "nombre" to name,
                                "apellidos" to surnames,
                                "carnet" to card,
                                "telefono" to phone,
                                "cuentaBancaria" to bankAccount,
                                "correo" to email
                            )

                            // Guardar en Firestore
                            firestore.collection("usuarios")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT)
                                        .show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error al guardar datos: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        } else {
                            Toast.makeText(
                                this,
                                task.exception?.message ?: "Error al registrar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}