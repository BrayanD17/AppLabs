package com.labs.applabs.administrator

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.labs.applabs.elements.ToastType
import com.labs.applabs.elements.toastMessage
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class DetailFormActivity : AppCompatActivity() {
    private lateinit var applicationOperatorTitle: TextView
    private lateinit var typeForm:TextView
    private var idForm: String? = null
    private var idFormOperator: String? = null
    private var idUser: String? = null
    private var urlApplication: String? = null
    private val provider: Provider = Provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_form)
        idForm="pHtKsliS3Zy3iGFvel3j"
        showInfo(idForm!!)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //Function to show form data of the user and the application
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
                studentSemester.text = "${studentInfo.studentSemester} @string/dataSemester"
                studentShifts.text = "${studentInfo.studentShifts} @string/hoursWeekly"
                studentAverage.text = studentInfo.studentAverage
                // Schedule availability
                val styleLetter = ResourcesCompat.getFont(this@DetailFormActivity, R.font.montserrat_light)
                scheduleAvailability.removeAllViews()
                studentInfo.scheduleAvailability.forEach { schedule ->
                    val textView = TextView(this@DetailFormActivity).apply {
                        text = schedule
                        textSize = 14f
                        typeface = styleLetter
                        setPadding(0, 8, 0, 8)
                    }
                    scheduleAvailability.addView(textView)
                }
                urlApplication = studentInfo.urlApplication
                downloadBoleta(urlApplication!!)

            } ?: run {
                studentCareer.text = "@string/dataNotAvailable"
                studentLastDigitCard.text = "@string/dataNotAvailable"
                studentId.text = "@string/dataNotAvailable"
                namePsycologist.text = "@string/dataNotAvailable"
                studentSemester.text = "@string/dataNotAvailable"
                studentShifts.text = "@string/dataNotAvailable"
                studentAverage.text = "@string/dataNotAvailable"
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
                studentName.text = "@string/dataNotAvailable"
                studentCard.text = "@string/dataNotAvailable"
                studentEmail.text = "@string/dataNotAvailable"
                studentPhone.text = "@string/dataNotAvailable"
                bankAccount.text = "@string/dataNotAvailable"
            }

            //Assign form name
            val formOperator = provider.getFormOperator(idFormOperator)
            formOperator?.let { form ->
                val dataformOperator = form.formOperator
                applicationOperatorTitle.text = dataformOperator.applicationOperatorTitle
                typeForm.text = "${dataformOperator.typeForm} ${dataformOperator.year}"
            } ?: run {
                applicationOperatorTitle.text = "@string/dataNotAvailable"
                typeForm.text = "@string/dataNotAvailable"
            }

        }

        //Update application status
        val statusAplication = findViewById<RadioGroup>(R.id.radioGroup)
        var dataComment = findViewById<EditText>(R.id.textDataComment)
        val btnUpdateStatus = findViewById<Button>(R.id.btnUpdateStatus)
        btnUpdateStatus.setOnClickListener {

        }


    }

    fun downloadBoleta(urlApplication: String) {
        val btnDescargar = findViewById<FrameLayout>(R.id.btnDescargarBoleta)
        btnDescargar.setOnClickListener {
            if (urlApplication.isNotEmpty()) {
                val request = DownloadManager.Request(Uri.parse(urlApplication))
                    .setTitle("Descargando documento")
                    .setDescription("Formulario PDF")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalFilesDir(
                        this,
                        Environment.DIRECTORY_DOWNLOADS,
                        "formulario.pdf"
                    )

                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
                toastMessage("Descarga iniciada", ToastType.SUCCESS)
            } else {
                toastMessage("No se encontró la URL del documento", ToastType.ERROR)
            }
        }

    }



}