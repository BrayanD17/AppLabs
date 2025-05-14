package com.labs.applabs.student

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class DetailsFormStudentActivity : AppCompatActivity() {

    private lateinit var applicationOperatorTitle: TextView
    private lateinit var typeForm:TextView
    private lateinit var formId: String
    private var idFormOperator: String? = null
    private var idUser: String? = null
    private var urlApplication: String? = null
    private val provider: Provider = Provider()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_form_student)
        val id = intent.getStringExtra("formIdStudent")
        if (id == null) {
            toastMessage("ID de formulario no recibido", ToastType.ERROR)
            finish()
            return
        }
        formId = id
        showInfo(formId)
        finishActivity()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun showInfo(formId: String) {
        applicationOperatorTitle = findViewById(R.id.textViewApplicationTitle)
        typeForm = findViewById(R.id.textViewTypeForm)
        val studentName = findViewById<TextView>(R.id.textDataInfoName)
        val studentId = findViewById<TextView>(R.id.textDataId)
        val studentCard = findViewById<TextView>(R.id.textDataCardStudent)
        val studentEmail = findViewById<TextView>(R.id.textDataEmail)
        val studentPhone = findViewById<TextView>(R.id.textDataPhone)
        val bankAccount = findViewById<TextView>(R.id.textDataBankAccount)
        val studentLastDigitCard = findViewById<TextView>(R.id.textDataCardDigit)
        val studentAverage = findViewById<TextView>(R.id.textDataAverage)
        val studentShifts = findViewById<TextView>(R.id.textDataShifts)
        val studentCareer = findViewById<TextView>(R.id.textDataCareer)
        val studentSemester = findViewById<TextView>(R.id.textDataOperatorSemester)
        val namePsycologist = findViewById<TextView>(R.id.textDataPsychology)
        val scheduleAvailability = findViewById<LinearLayout>(R.id.containerDataSchedule)
        val status = findViewById<TextView>(R.id.textStatusData)
        val comments = findViewById<TextView>(R.id.textCommentData)

        lifecycleScope.launch {
            //Assign form data that belongs to the user
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
                comments.text = studentInfo.comment
                status.text = studentInfo.statusApplication
                // Schedule availability
                val styleLetter = ResourcesCompat.getFont(this@DetailsFormStudentActivity, R.font.montserrat_light)
                scheduleAvailability.removeAllViews()
                studentInfo.scheduleAvailability.forEach { schedule ->
                    val textView = TextView(this@DetailsFormStudentActivity).apply {
                        text = "${schedule.day}: ${schedule.shift.joinToString(", ")}"
                        textSize = 14f
                        typeface = styleLetter
                        setPadding(0, 8, 0, 8)
                    }
                    scheduleAvailability.addView(textView)
                }

                urlApplication = studentInfo.urlApplication
                downloadBoleta(urlApplication!!)

            } ?: run {
                studentCareer.text = "No disponible"
                studentLastDigitCard.text = "No disponible"
                studentId.text = "No disponible"
                namePsycologist.text = "No disponible"
                studentSemester.text = "No disponible"
                studentShifts.text = "No disponible"
                studentAverage.text = "No disponible"
            }


            //Assign user data
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

            //Assign form name
            val formDataOperator = provider.getFormOperator(idFormOperator)
            formDataOperator?.let { form ->
                val dataformOperator = form.formOperator
                applicationOperatorTitle.text = dataformOperator.applicationOperatorTitle
                typeForm.text = "${dataformOperator.typeForm} ${dataformOperator.year}"
            } ?: run {
                applicationOperatorTitle.text = "No disponible"
                typeForm.text = "No disponible"
            }

        }
    }

    private fun downloadBoleta(urlApplication: String) {
        val fileName = Uri.parse(urlApplication).lastPathSegment?.substringAfterLast("/")?.substringBefore("?") ?: "archivo.pdf"
        val request = DownloadManager.Request(Uri.parse(urlApplication))
            .setTitle("Descargando documento")
            .setDescription(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(
                this,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        toastMessage("Descarga iniciada", ToastType.SUCCESS)
    }


    private fun finishActivity(){
        val backView = findViewById<ImageView>(R.id.backViewDetailStudent)
        backView.setOnClickListener {
            finish()
        }
    }
}