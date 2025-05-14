package com.labs.applabs.models

data class Usuario(
    val uid: String = "",
    val name: String = "",
    val surnames: String = "",
    val email: String = "",
    val phone: String = "",
    val bankAccount: String = "",
    val studentCard: String = "",
    val userRole: Int? = 2 // 1 Administrador, 2 Estudiante y 3 Operador
)

