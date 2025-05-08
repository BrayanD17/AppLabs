package com.labs.applabs.models

abstract class ValidadorCampos {

    //Solo dominios @itcr.ac.cr o @estudiantec.cr
    fun validarCorreo(correo: String): String? {
        if (correo.isBlank()) return "El correo no puede estar vacío"
        val regexCorreo = Regex("^[\\w.-]+@(itcr\\.ac\\.cr|estudiantec\\.cr)$")
        if (!regexCorreo.matches(correo)) {
            return "Correo no válido. Use @itcr.ac.cr o @estudiantec.cr"
        }
        return null
    }

    //Al menos una letra, un número y un carácter especial
    fun validarContrasena(contrasena: String): String? {
        val password = contrasena.trim()

        if (password.length < 6) return "Debe tener al menos 6 caracteres"

        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_.,+=<>?/]).{6,}$")

        if (!regex.matches(password)) {
            return "Debe contener al menos una letra, un número y un carácter especial"
        }

        return null
    }



    //Solo letras y espacios, mínimo 6 caracteres
    fun validarNombre(nombre: String): String? {
        if (nombre.length < 6) return "Debe tener mínimo 6 caracteres"
        val regex = Regex("^[a-zA-ZÁÉÍÓÚáéíóúÑñ\\s]{6,}\$")
        if (!regex.matches(nombre)) {
            return "Solo letras y espacios"
        }
        return null
    }

    //Formatos: +506 8736-6357 o 8736-6357
    fun validarTelefono(telefono: String): String? {
        val regex = Regex("^((\\+506\\s)?\\d{4}-\\d{4})\$")
        if (!regex.matches(telefono.trim())) {
            return "Formato válido: +506 8736-6357 o 8736-6357"
        }
        return null
    }

    //Cuenta bancaria de CR con números y guiones
    fun validarCuentaBancaria(cuenta: String): String? {
        val regex = Regex("^\\d{3}-\\d{6}-\\d{3}|\\d{9,20}$") // Ejemplo: 152-011215-001 o sin guiones
        if (!regex.matches(cuenta)) {
            return "Cuenta inválida. Use números y guiones como el formato estandar"
        }
        return null
    }

    //Solo 10 números exactos
    fun validarCarnet(carnet: String): String? {
        val regex = Regex("^\\d{10}\$")
        if (!regex.matches(carnet)) {
            return "El carnet debe tener exactamente 10 dígitos"
        }
        return null
    }
}
