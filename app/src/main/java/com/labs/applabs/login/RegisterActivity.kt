package com.labs.applabs

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.labs.applabs.models.ValidadorCampos

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSurnames: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBankAccount: EditText
    private lateinit var btnNext: Button

    // Instancia del validador centralizado
    private val validador = object : ValidadorCampos() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Referencias del layout
        etName = findViewById(R.id.etName)
        etSurnames = findViewById(R.id.etSurnames)
        etPhone = findViewById(R.id.etPhone)
        etBankAccount = findViewById(R.id.etBankAccount)
        btnNext = findViewById(R.id.btnRegister)

        btnNext.setOnClickListener {
            val nombre = etName.text.toString().trim()
            val apellidos = etSurnames.text.toString().trim()
            val telefono = etPhone.text.toString().trim()
            val cuenta = etBankAccount.text.toString().trim()

            // Validar nombre completo (nombre + apellidos)
            val nombreCompleto = "$nombre $apellidos"
            val errorNombre = validador.validarNombre(nombreCompleto)
            if (errorNombre != null) {
                etName.error = errorNombre
                etSurnames.error = errorNombre
                return@setOnClickListener
            }

            // Validar teléfono
            val errorTelefono = validador.validarTelefono(telefono)
            if (errorTelefono != null) {
                etPhone.error = errorTelefono
                return@setOnClickListener
            }

            // Validar cuenta bancaria
            val errorCuenta = validador.validarCuentaBancaria(cuenta)
            if (errorCuenta != null) {
                etBankAccount.error = errorCuenta
                return@setOnClickListener
            }

            // Si es valido avanza al siguiente
            val intent = Intent(this, RegisterActivity2::class.java).apply {
                putExtra("nombre", nombre)
                putExtra("apellidos", apellidos)
                putExtra("telefono", telefono)
                putExtra("cuenta", cuenta)
            }
            startActivity(intent)
        }
    }
}
