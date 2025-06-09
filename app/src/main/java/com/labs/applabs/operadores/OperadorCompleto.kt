package com.labs.applabs.operadores

data class OperadorCompleto(
    val userId: String,
    val carnet: String,
    val nombre: String,
    val carrera: String,
    // campos necesarios para el filtro de operadores activos
)