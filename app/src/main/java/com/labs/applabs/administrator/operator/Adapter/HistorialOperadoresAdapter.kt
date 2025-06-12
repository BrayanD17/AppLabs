package com.labs.applabs.administrator.operator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.operator.OperadorCompleto

class HistorialOperadoresAdapter(
    private var operadores: List<OperadorCompleto>
) : RecyclerView.Adapter<HistorialOperadoresAdapter.OperadorViewHolder>() {

    private var onClick: ((OperadorCompleto) -> Unit)? = null

    fun setOnItemClickListener(listener: (OperadorCompleto) -> Unit) {
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
        holder.nombre.text = operador.nombre
        holder.correo.text = operador.correo

    }

    override fun getItemCount(): Int = operadores.size

    fun actualizarLista(nuevaLista: List<OperadorCompleto>) {
        operadores = nuevaLista
        notifyDataSetChanged()
    }
}

