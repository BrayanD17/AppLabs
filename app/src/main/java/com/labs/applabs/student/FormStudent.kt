package com.labs.applabs.student

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labs.applabs.R


class FormStudent : AppCompatActivity() {

    lateinit var idCard : EditText
    lateinit var weightedAverage : EditText
    lateinit var degree : Spinner
    lateinit var phoneNumber: EditText
    var degreeSelected: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_student)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        spinner()
        idCard = findViewById(R.id.etIdStudent)
        weightedAverage = findViewById(R.id.etWeightedAverage)
        phoneNumber = findViewById(R.id.etPhoneNumber)


        // Rellenar si hay datos guardados
        if (FormStudentData.idCard.isNotEmpty()) {
            idCard.setText(FormStudentData.idCard)
            weightedAverage.setText(FormStudentData.weightedAverage.toString())
            phoneNumber.setText(FormStudentData.phoneNumber)
        }
    }

    fun spinner () {
        val degreeList = listOf("Computacion")
        degree = findViewById(R.id.spinnerDegree)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, degreeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        degree.adapter = adapter

        degree.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                degreeSelected = degreeList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(this@FormStudent, "Nada", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun validateFields(): Boolean {
        if (idCard == null || idCard.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "La cedula del estudiante es obligatoria", Toast.LENGTH_SHORT).show()
            return false
        }

        if (weightedAverage == null || weightedAverage.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "El promedio ponderado es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phoneNumber == null || phoneNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "El número telefonico es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun Siguiente(view: View){
        if (validateFields()){
            // Guardar en FormStudentData
            FormStudentData.idCard = idCard.text.toString()
            FormStudentData.weightedAverage = weightedAverage.text.toString().toFloat()
            FormStudentData.phoneNumber = phoneNumber.text.toString()
            FormStudentData.degree = degreeSelected

            val intent = Intent(this@FormStudent, FormStudent2::class.java)
            startActivity(intent)

        }
    }
}