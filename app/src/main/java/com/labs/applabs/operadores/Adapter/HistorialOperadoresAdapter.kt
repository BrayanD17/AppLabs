package com.labs.applabs.operadores.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.operadores.OperadorHistorial

class HistorialOperadoresAdapter(
    private var operadores: List<OperadorHistorial>
) : RecyclerView.Adapter<HistorialOperadoresAdapter.OperadorViewHolder>() {

    private var onClick: ((OperadorHistorial) -> Unit)? = null

    fun setOnItemClickListener(listener: (OperadorHistorial) -> Unit) {
        onClick = listener
    }

    inner class OperadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tvNombre)
        val correo: TextView = itemView.findViewById(R.id.tvCorreo)

        init {
            itemView.setOnClickListener {
                onClick?.invoke(operadores[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperadorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_operador, parent, false)
        return OperadorViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperadorViewHolder, position: Int) {
        val operador = operadores[position]
        holder.nombre.text = operador.nombreUsuario
        holder.correo.text = operador.correoUsuario
    }

    override fun getItemCount(): Int = operadores.size

    fun actualizarLista(nuevaLista: List<OperadorHistorial>) {
        operadores = nuevaLista
        notifyDataSetChanged()
    }
}
