package com.labs.applabs.administrator.operator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.historySemesterOperator

class AdapterHistorySemesterOperator (private var list: List<historySemesterOperator>) :
RecyclerView.Adapter<AdapterHistorySemesterOperator.ViewHolder>() {

    private var onItemClickListener: ((historySemesterOperator) -> Unit)? = null

    // public method to set the click listener
    fun setOnItemClickListener(listener: (historySemesterOperator) -> Unit) {
        this.onItemClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val type: TextView = itemView.findViewById(R.id.nameTextView)
        val date: TextView = itemView.findViewById(R.id.emailTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.type.text = "${item.semester} ${item.year}"
        holder.date.text = item.date

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<historySemesterOperator>) {
        list = newList
        notifyDataSetChanged()
    }
}