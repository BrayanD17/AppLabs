package com.labs.applabs.administrator.operator

data class OperadorCompleto(
    val userId: String = "",
    val carnet: String = "",
    val nombre: String = "",
    val carrera: String = "",
    var correo: String = "",
    val laboratorios: Map<String, Map<String, List<String>>> = emptyMap()
    //   ^--- Laboratorio -> Día -> Lista de horarios
)


