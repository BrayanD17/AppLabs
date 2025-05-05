package com.labs.applabs.administrator

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class DetailFormActivity : AppCompatActivity() {
    private lateinit var applicationOperatorTitle: TextView
    private lateinit var typeForm:TextView
    val provider: Provider = Provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_form)
        showInfo("gfTos90dNJeX8kkffqIo")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun showInfo(userId: String) {
        applicationOperatorTitle = findViewById(R.id.textViewApplicationTitle)
        typeForm = findViewById(R.id.textViewTypeForm)
        var studentName = findViewById<TextView>(R.id.textDataInfoName)
        var studentId = findViewById<TextView>(R.id.textDataId) //cedula
        var studentCard = findViewById<TextView>(R.id.textDataCardStudent) //carnet
        var studentEmail = findViewById<TextView>(R.id.textDataEmail)
        var studentPhone = findViewById<TextView>(R.id.textDataPhone)
        var bankAccount = findViewById<TextView>(R.id.textDataBankAccount)
        var studentLastDigitCard = findViewById<TextView>(R.id.textDataCardDigit)
        var studentAverage = findViewById<TextView>(R.id.textDataAverage) //promedio
        var studentShifts = findViewById<TextView>(R.id.textDataShifts) //turnos
        var studentCareer = findViewById<TextView>(R.id.textDataCareer)
        var studentSemester = findViewById<TextView>(R.id.textDataOperatorSemester)
        var namePsycologist = findViewById<TextView>(R.id.textDataPsychology)
        val scheduleAvailability = findViewById<LinearLayout>(R.id.containerDataSchedule)
        val btnDescargar = findViewById<FrameLayout>(R.id.btnDescargarBoleta)

        //Update application status
        val statusAplication = findViewById<RadioGroup>(R.id.radioGroup)
        var dataComment = findViewById<EditText>(R.id.textDataComment)
        val btnUpdateStatus = findViewById<Button>(R.id.btnUpdateStatus)
        btnUpdateStatus.setOnClickListener {
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.toast_succes, null)

            val text: TextView = layout.findViewById(R.id.toast_text)
            text.text = "Estado actualizado con éxito"

            val toast = Toast(applicationContext)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            toast.show()
        }


        lifecycleScope.launch {
            //Asignar datos del usuario
            val infoUser = provider.getUserInfo(userId)
            infoUser?.let { info ->
                val studentInfo = info.studentInfo
                studentName.text = "${studentInfo.studentName} ${studentInfo.surNames}"
                studentCard.text = studentInfo.studentCard
                studentEmail.text = studentInfo.studentEmail
                studentPhone.text = studentInfo.studentPhone
                bankAccount.text = studentInfo.bankAccount
            } ?: run {
                studentName.text = "No disponible"
                studentCard.text = "No disponible"
                studentEmail.text = "No disponible"
                studentPhone.text = "No disponible"
                bankAccount.text = "No disponible"
            }

            //Asignar datos del formulario que pertenece al usuario
            val formStudent = provider.getFormStudent(userId)
        }





// Lista de ejemplo, puedes reemplazar esto con datos reales de Firebase, Room, etc.
        val horarios = listOf("Lunes 9-11", "Martes 14-16", "Viernes 10-12", "Viernes 10-12","Viernes 10-12","Viernes 10-12","Viernes 10-12")
        val tipoLetra = ResourcesCompat.getFont(this, R.font.montserrat_light)
        for (horario in horarios) {
            val textView = TextView(this).apply {
                text = horario
                textSize = 14f
                typeface = tipoLetra
                setPadding(0, 8, 0, 8)
            }
            scheduleAvailability.addView(textView)
        }


    }

    fun downloadBoleta(userId: String) {}





}