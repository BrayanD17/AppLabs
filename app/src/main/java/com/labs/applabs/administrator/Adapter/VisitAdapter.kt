package com.labs.applabs.administrator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.ReportVisit

class VisitAdapter(private var visits: List<ReportVisit>) : RecyclerView.Adapter<VisitAdapter.VisitViewHolder>() {


    class VisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvNameStudentVisit: TextView = itemView.findViewById(R.id.tvNameStudentVisit)
        val tvIdStudentVisit: TextView = itemView.findViewById(R.id.tvIdStudentVisit)
        val tvLaboratoryVisit: TextView = itemView.findViewById(R.id.tvLaboratoryVisit)
        val tvDateVisit: TextView = itemView.findViewById(R.id.tvDateVisit)
        val tvStartTime : TextView = itemView.findViewById(R.id.tvStartTime)
        val tvEndTime : TextView = itemView.findViewById(R.id.tvEndTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visit, parent, false)
        return VisitViewHolder(view)
    }

    override fun getItemCount(): Int = visits.size

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        holder.tvNameStudentVisit.text = visits[position].student
        holder.tvIdStudentVisit.text = visits[position].cardStudent
        holder.tvLaboratoryVisit.text = "Laboratorio: ${visits[position].laboratory}"
        holder.tvDateVisit.text = "Fecha: ${visits[position].date}"
        holder.tvStartTime.text = "Hora de entrada: ${visits[position].startTime}"
        holder.tvEndTime.text = "Hora de salida: ${visits[position].endTime}"
    }

    fun updateData(newVisits: List<ReportVisit>) {
        visits = newVisits
        notifyDataSetChanged()

    }
}