package com.labs.applabs.elements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.elements.FiltroDialogFragment.FilterListener
import com.labs.applabs.firebase.FilterData
import com.labs.applabs.firebase.FilterShowAllOperator
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class FiltroDialogShowAllOperator : DialogFragment() {

    val provider: Provider = Provider()

    interface FilterListener {
        fun onFilterApply(filterData: FilterShowAllOperator)
        fun onFilterCancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_filter_show_all_operator, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etCarnet = view.findViewById<EditText>(R.id.et_carnet)
        val etEmail= view.findViewById<EditText>(R.id.et_email)
        val etPhone= view.findViewById<EditText>(R.id.et_phone)

        val btnAplicar = view.findViewById<Button>(R.id.btn_aplicar)
        val btnReiniciar = view.findViewById<Button>(R.id.btn_reiniciar)

        btnAplicar.setOnClickListener {
            // Obtener los valores actuales de los EditText cuando se hace clic
            val carnetInput = etCarnet.text.toString().takeIf { it.isNotBlank() }
            val emailInput = etEmail.text.toString().takeIf { it.isNotBlank() }
            val phoneInput = etPhone.text.toString().takeIf { it.isNotBlank() }

            val filterData = FilterShowAllOperator(
                carnetOperator = carnetInput,
                emailOperator = emailInput,
                phoneOperator = phoneInput
            )
            (activity as? FilterListener)?.onFilterApply(filterData)
            dismiss()
        }

        btnReiniciar.setOnClickListener {
            etPhone.text.clear()
            etCarnet.text.clear()
            etEmail.text.clear()
            (activity as? com.labs.applabs.elements.FiltroDialogShowAllOperator.FilterListener)?.onFilterCancel()
            dismiss()
        }
    }
}