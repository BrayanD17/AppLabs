package com.labs.applabs.administrator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.Solicitud

class SolicitudAdapter(private var lista: List<Solicitud>) :
    RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    private var listener: ((Solicitud) -> Unit)? = null

    fun setOnItemClickListener(listener: (Solicitud) -> Unit) {
        this.listener = listener
    }

    class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.nameTextView)
        val email: TextView = itemView.findViewById(R.id.emailTextView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item, parent, false)
        return SolicitudViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val item = lista[position]
        holder.name.text = item.nombre
        holder.email.text = item.correo


        holder.itemView.setOnClickListener {
            listener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Solicitud>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
