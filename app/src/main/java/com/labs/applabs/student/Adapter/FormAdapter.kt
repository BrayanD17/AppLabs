package com.labs.applabs.student.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.labs.applabs.R
import com.labs.applabs.student.Form

class FormAdapter (private val forms : List<Form>) : RecyclerView.Adapter<FormAdapter.ViewHolder>() {


    class ViewHolder(view: View) :RecyclerView.ViewHolder(view){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    }

    override fun getItemCount(): Int = forms.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}