package com.labs.applabs.firebase

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.models.Usuario

object FirebaseUsuarioService {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Registra usuario en FirebaseAuth y Firestore
    fun registrarUsuario(
        context: Context,
        correo: String,
        password: String,
        usuario: Usuario,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val usuarioFinal = usuario.copy(uid = uid ?: "")

                    // Guarda en la colección 'usuarios'
                    db.collection("usuarios").document(uid!!)
                        .set(usuarioFinal)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError("Error al guardar: ${e.message}")
                        }
                } else {
                    onError("Error de registro: ${task.exception?.message}")
                }
            }
    }

    // Obtiene el rol del usuario por UID
    fun verificarRol(uid: String, onResult: (Int) -> Unit) {
        db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val rol = doc.getLong("rol")?.toInt() ?: 3 // 3 = estudiante
                onResult(rol)
            }
            .addOnFailureListener {
                onResult(3)
            }
    }
}
