package com.labs.applabs.student.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.FormListStudent

class FormListAdapter(private var lista: List<FormListStudent>) :
    RecyclerView.Adapter<FormListAdapter.FormListStudentViewHolder>() {

    private var itemClickListener: ((FormListStudent) -> Unit)? = null
    private var editClickListener: ((FormListStudent) -> Unit)? = null
    private var deleteClickListener: ((FormListStudent) -> Unit)? = null

    fun setOnItemClickListener(listener: (FormListStudent) -> Unit) {
        this.itemClickListener = listener
    }

    fun setOnEditClickListener(listener: (FormListStudent) -> Unit) {
        editClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (FormListStudent) -> Unit) {
        deleteClickListener = listener
    }

    class FormListStudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val semester: TextView = itemView.findViewById(R.id.textSemester)
        val formName: TextView = itemView.findViewById(R.id.textFormName)
        val dateEnd: TextView = itemView.findViewById(R.id.textDateEnd)
        val editIcon: ImageView = itemView.findViewById(R.id.iconEditFormStudent) // ícono de editar
        val deleteIcon: ImageView = itemView.findViewById(R.id.iconDeleteFormStudent) // ícono de eliminar
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormListStudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_form_student, parent, false)
        return FormListStudentViewHolder(view)
    }


    override fun onBindViewHolder(holder: FormListStudentViewHolder, position: Int) {
        val item = lista[position]
        holder.semester.text = item.Semester
        holder.formName.text = item.FormName
        holder.dateEnd.text = item.DateEnd


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

    fun actualizarLista(nuevaLista: List<FormListStudent>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
