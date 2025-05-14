package com.labs.applabs.student.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.getMessage

class NotificationAdapter(private val items: List<getMessage>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subject: TextView = itemView.findViewById<TextView>(R.id.tvTitle)
        val message: TextView = itemView.findViewById(R.id.tvMessage)
        val date: TextView = itemView.findViewById<TextView>(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_view, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = items[position]
        holder.subject.text = notification.subject
        holder.message.text = notification.infomessage
        holder.date.text = notification.timestamp
    }

    override fun getItemCount(): Int = items.size
}
