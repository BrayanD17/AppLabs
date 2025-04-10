package com.labs.applabs
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.labs.applabs.login.RegisterActivity
import com.labs.applabs.administrator.DetailFormActivity
import com.labs.applabs.R


class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        // Vincular los elementos del layout
        emailEditText = findViewById(R.id.et1)
        passwordEditText = findViewById(R.id.et2)
        loginButton = findViewById(R.id.btn2)
        registerButton = findViewById(R.id.btn3)

        // Navegar a la pantalla de registro
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Iniciar sesión
        /*loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val pass = passwordEditText.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        // Aquí podría navegar a otra actividad en este caso a la vista después de logueada
                    } else {
                        Toast.makeText(this, it.exception?.message ?: "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No se permiten campos vacíos", Toast.LENGTH_SHORT).show()
            }
        }*/
    }

   /* override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            Toast.makeText(this, "Sesión iniciada automáticamente", Toast.LENGTH_SHORT).show()
            // Si ya estoy logueada saltar a la vista siguiente despues de log in
        }
    }*/

    fun activy(view:View){
        val intent: Intent=Intent(this@MainActivity,com.labs.applabs.administrator.DetailFormActivity::class.java)
        startActivity(intent)
    }
}