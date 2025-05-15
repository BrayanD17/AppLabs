package com.labs.applabs.administrator.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.labs.applabs.R
import com.labs.applabs.models.FormOperador

class FormOperadorAdapter(
    private val context: Context,
    private val formularios: List<FormOperador>
) : BaseAdapter() {

    override fun getCount(): Int = formularios.size

    override fun getItem(position: Int): Any = formularios[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.request_item, parent, false)

        val nombreText = view.findViewById<TextView>(R.id.nameTextView)
        val emailText = view.findViewById<TextView>(R.id.emailTextView)

        val form = formularios[position]

        nombreText.text = form.nameForm
        emailText.text = "Periodo: ${form.semester}  |  Año: ${form.year}"

        return view
    }
}
