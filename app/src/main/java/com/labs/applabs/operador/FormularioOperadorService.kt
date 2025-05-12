package com.labs.applabs.operador

import com.google.firebase.firestore.FirebaseFirestore

class FormularioOperadorService {

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("formOperator")

    fun createFormulario(formulario: FormOperador, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Genera un ID único automáticamente
        collectionRef
            .add(formulario)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}