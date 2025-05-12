package com.labs.applabs.models

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val cuentaBancaria: String = "",
    val carnet: String = "",
    val rol: Int? = null //sin rol (se interpreta como estudiante), hasta que el administrador le asigne
)

