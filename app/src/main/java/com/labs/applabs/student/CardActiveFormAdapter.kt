package com.labs.applabs.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.formOperatorActive

class CardActiveFormAdapter(private val studentList: List<formOperatorActive>) :
    RecyclerView.Adapter<CardActiveFormAdapter.StudentViewHolder>() {

    private var itemClickListener: ((formOperatorActive) -> Unit)? = null

    fun setOnItemClickListener(listener: (formOperatorActive) -> Unit) {
        itemClickListener = listener
    }

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNameForm: TextView = itemView.findViewById(R.id.nameFormActive)
        val textSemester: TextView = itemView.findViewById(R.id.semesterActive)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_form_active, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentList[position]
        holder.textNameForm.text = student.nameActiveForm
        holder.textSemester.text = student.semesterActive

        holder.itemView.setOnClickListener {
            itemClickListener?.invoke(student)
        }
    }

    override fun getItemCount(): Int = studentList.size
}
