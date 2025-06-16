package com.labs.applabs.elements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.filterDataVisit
import kotlinx.coroutines.launch

class FiltroDialogVisits : DialogFragment() {

    val provider: Provider = Provider()
    private var date: EditText? = null
    private lateinit var labSpinner: Spinner
    private var laboratorySelected: String? = ""

    interface FilterListener {
        fun onFilterApply(filterData: filterDataVisit)
        fun onFilterCancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_filters_visit, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun loadSpinnerLaboratories() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val statusList = provider.getLaboratoryName()
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    statusList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                labSpinner.adapter = adapter

                labSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        laboratorySelected = statusList[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        Toast.makeText(requireContext(), "Seleccione un laboratorio", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar laboratorio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        labSpinner = view.findViewById(R.id.spinnerLaboratory)
        date = view.findViewById(R.id.editFecha)
        val etCarnet = view.findViewById<EditText>(R.id.etCarnet)
        val btnAplicar = view.findViewById<Button>(R.id.btn_aplicar)
        val btnReiniciar = view.findViewById<Button>(R.id.btn_reiniciar)

        loadSpinnerLaboratories()

        btnAplicar.setOnClickListener {
            val filterData = filterDataVisit(
                cardStudent = etCarnet.text.toString(),
                laboratory = laboratorySelected,
                date = date?.text.toString()
            )
            (activity as? FilterListener)?.onFilterApply(filterData)
            dismiss()
        }

        btnReiniciar.setOnClickListener {
            etCarnet.text.clear()
            date?.text?.clear()
            labSpinner.setSelection(0)
            (activity as? FilterListener)?.onFilterCancel()
            dismiss()
        }
    }


}
