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



    private var itemClickListener: ((Solicitud) -> Unit)? = null
    private var editClickListener: ((Solicitud) -> Unit)? = null
    private var deleteClickListener: ((Solicitud) -> Unit)? = null


    fun setOnItemClickListener(listener: (Solicitud) -> Unit) {
        this.listener = listener
    }

    fun setOnEditClickListener(listener: (Solicitud) -> Unit) {
        editClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (Solicitud) -> Unit) {
        deleteClickListener = listener
    }

    class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.nameTextView)
        val email: TextView = itemView.findViewById(R.id.emailTextView)
        val uid : String = ""
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
            itemClickListener?.invoke(item)
        }
        holder.editIcon.setOnClickListener {
            editClickListener?.invoke(item)
        }

        holder.deleteIcon.setOnClickListener {
            deleteClickListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Solicitud>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
