package com.labs.applabs.elements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.FilterDataMisconduct
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class FiltroDialogMisconduct : DialogFragment() {

    val provider: Provider = Provider()
    private lateinit var labSpinner: Spinner
    private lateinit var radioFirstSemester: RadioButton
    private lateinit var radioSecondSemester: RadioButton
    private var laboratorySelected: String? = null
    private var semesterSelected: String? = null



    interface FilterListener {
        fun onFilterApply(filterData: FilterDataMisconduct)
        fun onFilterCancel()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_filters_misconduct, container, false)
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

        labSpinner = view.findViewById(R.id.spinnerLaboratory)
        radioFirstSemester = view.findViewById(R.id.btnFirstSemesterMisconduct)
        radioSecondSemester = view.findViewById(R.id.btnSecondSemesterMisconduct)
        val etCarnet = view.findViewById<EditText>(R.id.etCarnet)
        val btnAplicar = view.findViewById<Button>(R.id.btn_aplicar)
        val btnReiniciar = view.findViewById<Button>(R.id.btn_reiniciar)


        loadSpinnerLaboratories()

        btnAplicar.setOnClickListener {
            semesterSelected = when {
                radioFirstSemester.isChecked -> getString(R.string.semester1)
                radioSecondSemester.isChecked -> getString(R.string.semester2)
                else -> null
            }

            val filterData = FilterDataMisconduct(
                carnet = etCarnet.text.toString(),
                semestres = semesterSelected,
                laboratory = laboratorySelected
            )
            (activity as? FilterListener)?.onFilterApply(filterData)
            dismiss()
        }

        btnReiniciar.setOnClickListener {
            radioFirstSemester.isChecked = true
            radioSecondSemester.isChecked = false
            labSpinner.setSelection(0)
            (activity as? FilterListener)?.onFilterCancel()
            dismiss()
        }


    }


    private fun loadSpinnerLaboratories() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val statusList = provider.getLaboratoryName()
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusList)
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
            } catch (e : Exception){
                Toast.makeText(requireContext(), "Error al cargar laboratorio", Toast.LENGTH_SHORT).show()
            }
        }
    }

}