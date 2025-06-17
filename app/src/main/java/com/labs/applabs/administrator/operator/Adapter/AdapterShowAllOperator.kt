package com.labs.applabs.administrator.operator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.OperatorItem

class AdapterShowAllOperator(private var list: List<OperatorItem>) :
    RecyclerView.Adapter<AdapterShowAllOperator.ViewHolder>() {

    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        this.onItemClickListener = listener
    }

    fun update(newList: List<OperatorItem>) {
        list = newList
        notifyDataSetChanged()
    }

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
        val operatorItem = list[position]
        val studentInfo = operatorItem.data.studentInfo

        holder.name.text = "${studentInfo.studentName} ${studentInfo.surNames}"
        holder.email.text = studentInfo.studentEmail

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(operatorItem.userId)
        }
    }

    override fun getItemCount(): Int = list.size
}
