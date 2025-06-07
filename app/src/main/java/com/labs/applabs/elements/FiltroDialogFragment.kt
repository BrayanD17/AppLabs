package com.labs.applabs.elements

import android.os.Bundle
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
import com.labs.applabs.firebase.FilterData
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage

class FiltroDialogFragment : DialogFragment() {

    val provider: Provider = Provider()
    private lateinit var degreeSpinner: Spinner
    private lateinit var statusSpinner: Spinner
    private var degreeSelected: String? = null
    private var statusSelected: String? = null

    interface FilterListener {
        fun onFilterApply(filterData: FilterData)
        fun onFilterCancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filtros_dialog, container, false)
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

        degreeSpinner = view.findViewById(R.id.spinner_carrera)
        statusSpinner = view.findViewById(R.id.spinner_estado)

        val etSemestres = view.findViewById<EditText>(R.id.et_semestres)
        val etNombre = view.findViewById<EditText>(R.id.et_nombre)
        val etCarnet = view.findViewById<EditText>(R.id.et_carnet)


        val btnAplicar = view.findViewById<Button>(R.id.btn_aplicar)
        val btnReiniciar = view.findViewById<Button>(R.id.btn_reiniciar)

        btnAplicar.setOnClickListener {
            val filterData = FilterData(
                carrera = degreeSelected ?: "",
                semestres = etSemestres.text.toString(),
                nombre = etNombre.text.toString(),
                carnet = etCarnet.text.toString(),
                estado = (statusSpinner ?: "").toString()
            )
            (activity as? FilterListener)?.onFilterApply(filterData)
            dismiss()
        }

        btnReiniciar.setOnClickListener {
            degreeSpinner.setSelection(0)
            etSemestres.text.clear()
            etNombre.text.clear()
            etCarnet.text.clear()
            statusSpinner.setSelection(0)
            (activity as? FilterListener)?.onFilterCancel()
            dismiss()
        }

        // Llama al cargador del spinner
        loadSpinnerCarreras()
        loadSpinnerEstados()
    }

    private fun loadSpinnerCarreras() {
        // Corrutina segura para Fragment
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val degreeList = provider.getCareerNames()
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    degreeList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                degreeSpinner.adapter = adapter

                degreeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        degreeSelected = degreeList[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        Toast.makeText(requireContext(), "Seleccione una carrera", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar carreras", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSpinnerEstados() {
       viewLifecycleOwner.lifecycleScope.launch {
           try {
               val statusList = provider.getFormStatusData()
               val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusList)
               adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
               statusSpinner.adapter = adapter

               statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                   override fun onItemSelected(
                       parent: AdapterView<*>?,
                       view: View?,
                       position: Int,
                       id: Long
                   ) {
                       statusSelected = statusList[position]
                   }

                   override fun onNothingSelected(parent: AdapterView<*>?) {
                       Toast.makeText(requireContext(), "Seleccione un estado", Toast.LENGTH_SHORT).show()
                   }
               }
           } catch (e : Exception){
               Toast.makeText(requireContext(), "Error al cargar estados", Toast.LENGTH_SHORT).show()
           }
       }
    }



}

