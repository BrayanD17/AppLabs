package com.labs.applabs.models

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val cuentaBancaria: String = "",
    val carnet: String = "",
    val rol: Int? = 2 // 1 Administrador, 2 Estudiante y 3 Operador
)

