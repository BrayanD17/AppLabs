package com.labs.applabs.administrator.operator

data class OperadorCompleto(
    val userId: String,
    val carnet: String,
    val nombre: String,
    val carrera: String,
    val correo: String
    // campos necesarios para el filtro de operadores activos
)