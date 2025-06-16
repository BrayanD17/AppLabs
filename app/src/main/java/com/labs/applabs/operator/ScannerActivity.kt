package com.labs.applabs.operator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.zxing.integration.android.IntentIntegrator
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.ReportVisitStudent
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScannerActivity : AppCompatActivity() {

    val provider = Provider()
    private lateinit var laboratory: AutoCompleteTextView
    private var selectedLaboratory: String = ""
    private lateinit var switch : SwitchCompat

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        laboratory = findViewById(R.id.spinnerLaboratory)
        switch = findViewById(R.id.customSwitch)

        val backViewFormActivyty = findViewById<ImageView>(R.id.backViewFormActivyty)
        backViewFormActivyty.setOnClickListener {
            finish()
        }
        loadSpinnerLaboratory()

        val btnScan = findViewById<Button>(R.id.btnScan)
        btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Escanea el código del carné")
            integrator.setOrientationLocked(false)
            integrator.setBeepEnabled(true)
            integrator.initiateScan()
        }

        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {

                // Save result (Eje: 2021055077)
                val studentCard = result.contents
                val formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                val date = LocalDate.now().format(formatterDate).toString()
                val time = LocalTime.now().format(formatterTime).toString()
                Log.e("Tiempo", time)

                lifecycleScope.launch {

                    val idStudent = provider.getStudentIdCarne(studentCard)
                    val data = ReportVisitStudent(
                        idOperator = "idOperator",
                        idstudent = idStudent.toString(),
                        laboratory = selectedLaboratory,
                        date = date,
                        startTime = time,
                        endTime = time
                    )

                    if (switch.isChecked){
                        provider.updateEndTime(idStudent.toString(), date)
                        toastMessage("Registro actualizado", ToastType.SUCCESS)
                    } else {
                        provider.saveReportVisitStudent(data)
                        toastMessage("Registro guardado", ToastType.SUCCESS)
                    }
                }
            } else {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSpinnerLaboratory(){
        lifecycleScope.launch {
            try {
                val laboratoryList = provider.getLaboratoryName()
                val adapter = ArrayAdapter(this@ScannerActivity, android.R.layout.simple_dropdown_item_1line, laboratoryList)
                laboratory.setAdapter(adapter)
                laboratory.setOnItemClickListener { parent, view, position, id ->
                    selectedLaboratory = laboratoryList[position]
                }
            } catch (e : Exception){
                toastMessage("Error al cargar los datos", ToastType.ERROR)
            }
        }
    }
}