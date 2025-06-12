package com.labs.applabs.administrator.operator

data class OperadorHistorial(
    val userId: String = "",
    val formId: String = "",
    val semestre: String = "",
    val nombreFormulario: String = "",
    val fechaRegistro: com.google.firebase.Timestamp? = null,
    val nombreUsuario: String = "",
    val correoUsuario: String = ""
)
