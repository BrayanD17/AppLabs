package com.labs.applabs.administrator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.MisconductStudent

class MisconductAdapter (private var misconductList: List<MisconductStudent>) : RecyclerView.Adapter<MisconductAdapter.MisconductViewHolder>() {

    class MisconductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val studentNameTextView: TextView = itemView.findViewById(R.id.tvNameMisconduct)
        val emailTextView: TextView = itemView.findViewById(R.id.tvEmailMisconduct)
        val semesterTextView: TextView = itemView.findViewById(R.id.tvSemesterMisconduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MisconductViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_misconduct, parent, false)
        return MisconductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MisconductViewHolder, position: Int) {
        val misconduct = misconductList[position]
        holder.studentNameTextView.text = misconduct.student
        holder.emailTextView.text = misconduct.email
        holder.semesterTextView.text = misconduct.semester
    }

    override fun getItemCount(): Int = misconductList.size

    fun updateList(newList: List<MisconductStudent>) {
        misconductList = newList
        notifyDataSetChanged()
    }
}