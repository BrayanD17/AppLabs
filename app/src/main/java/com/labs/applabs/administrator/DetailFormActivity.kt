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
    private var idFormOperator: String? = null
    private var idUser: String? = null
    private val provider: Provider = Provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_form)
        showInfo("pHtKsliS3Zy3iGFvel3j")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun showInfo(formId: String) {
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


        lifecycleScope.launch {
            //Asignar datos del formulario que pertenece al usuario
            val formStudent = provider.getFormStudent(formId)
            formStudent?.let { form ->
                val studentInfo = form.studentInfo
                studentCareer.text = studentInfo.studentCareer
                studentLastDigitCard.text = studentInfo.studentLastDigitCard
                studentId.text = studentInfo.studentId
                idFormOperator = studentInfo.idFormOperator
                idUser = studentInfo.idUser
                namePsycologist.text = studentInfo.namePsycologist
                studentSemester.text = "${studentInfo.studentSemester} semestres"
                studentShifts.text = "${studentInfo.studentShifts} horas semanales"
                studentAverage.text = studentInfo.studentAverage
                // Mostrar horarios disponibles
                val tipoLetra =
                    ResourcesCompat.getFont(this@DetailFormActivity, R.font.montserrat_light)
                scheduleAvailability.removeAllViews()
                studentInfo.scheduleAvailability.forEach { horario ->
                    val textView = TextView(this@DetailFormActivity).apply {
                        text = horario
                        textSize = 14f
                        typeface = tipoLetra
                        setPadding(0, 8, 0, 8)
                    }
                    scheduleAvailability.addView(textView)
                }

            } ?: run {
                studentCareer.text = "No disponible"
                studentLastDigitCard.text = "No disponible"
                studentId.text = "No disponible"
                namePsycologist.text = "No disponible"
                studentSemester.text = "No disponible"
                studentShifts.text = "No disponible"
                studentAverage.text = "No disponible"
            }


            //Asignar datos del usuario
            val infoUser = provider.getUserInfo(idUser)
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

            //Asignar nombre del formulario
            val formOperator = provider.getFormOperator(idFormOperator)
            formOperator?.let { form ->
                val formOperator = form.formOperator
                applicationOperatorTitle.text = formOperator.applicationOperatorTitle
                typeForm.text = "${formOperator.typeForm} ${formOperator.year}"
            } ?: run {
                applicationOperatorTitle.text = "No disponible"
                typeForm.text = "No disponible"
            }

        }

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


    }
    fun downloadBoleta(userId: String) {}





}