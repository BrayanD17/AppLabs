package com.labs.applabs.administrator.operator.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.operator.OperadorCompleto
import com.labs.applabs.administrator.operator.viewInformationOperator

class HistorialOperadoresAdapter(
    private var operadores: List<OperadorCompleto>
) : RecyclerView.Adapter<HistorialOperadoresAdapter.OperadorViewHolder>() {

    private var onClick: ((OperadorCompleto) -> Unit)? = null

    inner class OperadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tvNombre)
        val correo: TextView = itemView.findViewById(R.id.tvCorreo)

        init {
            itemView.setOnClickListener {
                val operador = operadores[adapterPosition]
                val context = itemView.context

                val intent = Intent(context, viewInformationOperator::class.java)
                intent.putExtra("operatorId", operador.userId)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperadorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operator_history, parent, false)
        return OperadorViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperadorViewHolder, position: Int) {
        val operador = operadores[position]
        holder.nombre.text = operador.nombre ?: "(Sin nombre)"
        holder.correo.text = operador.correo ?: "(Sin email)"

    }

    override fun getItemCount(): Int = operadores.size

    fun actualizarLista(nuevaLista: List<OperadorCompleto>) {
        operadores = nuevaLista
        notifyDataSetChanged()
    }
    fun formatearLaboratorios(labs: Map<String, Map<String, List<String>>>): String {
        if (labs.isEmpty()) return "Sin asignaciones"
        return labs.entries.joinToString("\n") { (lab, dias) ->
            val diasHorarios = dias.entries.joinToString { (dia, horarios) ->
                "$dia: ${horarios.joinToString()}"
            }
            "$lab -> $diasHorarios"
        }
    }

}

