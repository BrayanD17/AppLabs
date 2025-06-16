package com.labs.applabs.administrator.operator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.DataClass

class AdapterShowAllOperator (private var list: List<DataClass>) :
    RecyclerView.Adapter<AdapterShowAllOperator.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.nameTextView)
        val email: TextView = itemView.findViewById(R.id.emailTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position].studentInfo
        holder.name.text = "${item.studentName} ${item.surNames}"
        holder.email.text = item.studentEmail
    }

    override fun getItemCount(): Int = list.size

    fun update(newList: List<DataClass>) {
        list = newList
        notifyDataSetChanged()
    }
}
