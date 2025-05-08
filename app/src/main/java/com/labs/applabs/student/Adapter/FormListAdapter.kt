package com.labs.applabs.student.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.FormListStudent

class FormListAdapter(private var lista: List<FormListStudent>) :
    RecyclerView.Adapter<FormListAdapter.FormListStudentViewHolder>() {

    private var listener: ((FormListStudent) -> Unit)? = null

    fun setOnItemClickListener(listener: (FormListStudent) -> Unit) {
        this.listener = listener
    }

    class FormListStudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val semester: TextView = itemView.findViewById(R.id.textSemester)
        val formName: TextView = itemView.findViewById(R.id.textFormName)
        val dateEnd: TextView = itemView.findViewById(R.id.textDateEnd)
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
            listener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<FormListStudent>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
