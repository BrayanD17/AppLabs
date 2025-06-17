package com.labs.applabs.administrator

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.*
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
import com.labs.applabs.firebase.dataUpdateStatus
import com.labs.applabs.student.studentMenuActivity
import kotlinx.coroutines.launch


class DetailFormActivity : AppCompatActivity() {
    private lateinit var applicationOperatorTitle: TextView
    private lateinit var typeForm: TextView
    private lateinit var formStudentId: String
    private var formIdOperator: String? = null
    private var idUser: String? = null
    private var urlApplication: String? = null
    private var comment: String? = null
    private var statusApplication: String? = null
    private var nameFormOperator: String? = null
    private var semesterFormOperator: String? = null
    private var studentNameValue: String = ""
    private var studentEmailValue: String = ""
    private val provider: Provider = Provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_form)

        val id = intent.getStringExtra("formId")
        if (id == null) {
            toastMessage("ID de formulario no recibido", ToastType.ERROR)
            finish()
            return
        }
        formStudentId = id
        showInfo(formStudentId)
        finishActivityDetailsForm()

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

        lifecycleScope.launch {
            val formStudent = provider.getFormStudent(formId)
            formStudent?.let { form ->
                val studentInfo = form.studentInfo
                studentCareer.text = studentInfo.studentCareer
                studentLastDigitCard.text = studentInfo.studentLastDigitCard
                studentId.text = studentInfo.studentId
                formIdOperator = studentInfo.idFormOperator
                idUser = studentInfo.idUser
                namePsycologist.text = studentInfo.namePsycologist
                studentSemester.text = "${studentInfo.studentSemester} semestres"
                studentShifts.text = "${studentInfo.studentShifts} horas semanales"
                studentAverage.text = studentInfo.studentAverage

                val styleLetter =
                    ResourcesCompat.getFont(this@DetailFormActivity, R.font.montserrat_light)
                scheduleAvailability.removeAllViews()
                studentInfo.scheduleAvailability.forEach { schedule ->
                    val textView = TextView(this@DetailFormActivity).apply {
                        text = "${schedule.day}: ${schedule.shift.joinToString(", ")}"
                        textSize = 14f
                        typeface = styleLetter
                        setPadding(0, 8, 0, 8)
                    }
                    scheduleAvailability.addView(textView)
                }

                urlApplication = studentInfo.urlApplication
                downloadBoleta(urlApplication!!)
                val dataComment = findViewById<EditText>(R.id.textDataComment)
                comment = studentInfo.comment
                dataComment.setText(comment)

                val statusRadioGroup = findViewById<RadioGroup>(R.id.radioGroup)
                when (studentInfo.statusApplication) {
                    "0" -> statusRadioGroup.check(R.id.radioStatusPending)
                    "1" -> statusRadioGroup.check(R.id.radioStatusAcept)
                    "2" -> statusRadioGroup.check(R.id.radioStatusRejected)
                    else -> statusRadioGroup.clearCheck()
                }
                statusApplication = studentInfo.statusApplication
            }

            val infoUser = provider.getUserInfo(idUser)
            infoUser?.let { info ->
                val studentInfo = info.studentInfo
                studentNameValue = "${studentInfo.studentName} ${studentInfo.surNames}"
                studentEmailValue = studentInfo.studentEmail
                studentName.text = studentNameValue
                studentCard.text = studentInfo.studentCard
                studentEmail.text = studentEmailValue
                studentPhone.text = studentInfo.studentPhone
                bankAccount.text = studentInfo.bankAccount
            }

            val formDataOperator = provider.getFormOperator(formIdOperator)
            formDataOperator?.let { form ->
                val dataformOperator = form.formOperator
                nameFormOperator = dataformOperator.nameForm
                semesterFormOperator = "${dataformOperator.typeForm} ${dataformOperator.year}"
                applicationOperatorTitle.text = dataformOperator.nameForm
                typeForm.text = semesterFormOperator
            }
        }

        val btnUpdateStatus = findViewById<Button>(R.id.btnUpdateStatus)
        btnUpdateStatus.setOnClickListener {
            updateApplicationStatus(
                idUser!!,
                comment!!,
                statusApplication!!,
                nameFormOperator!!,
                semesterFormOperator!!
            )
        }
    }

    private fun downloadBoleta(urlApplication: String) {
        val btnDescargar = findViewById<FrameLayout>(R.id.btnDownloadApplication)
        val fileName =
            Uri.parse(urlApplication).lastPathSegment?.substringAfterLast("/")?.substringBefore("?")
                ?: "archivo.pdf"
        btnDescargar.setOnClickListener {
            if (urlApplication.isNotEmpty()) {
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
            } else {
                toastMessage("No se encontró la URL del documento", ToastType.ERROR)
            }
        }
    }

    private fun updateApplicationStatus(
        userId: String,
        originalComment: String,
        originalStatus: String,
        nameFormOperator: String,
        semesterFormOperator: String
    ) {
        val dataComment = findViewById<EditText>(R.id.textDataComment)
        var commentText = dataComment.text.toString().trim()
        var newMessage: String

        val statusRadioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val selectedStatusId = statusRadioGroup.checkedRadioButtonId
        val statusText = when (selectedStatusId) {
            R.id.radioStatusPending -> "0"
            R.id.radioStatusAcept -> "1"
            R.id.radioStatusRejected -> "2"
            else -> "0"
        }

        val isStatusChanged = statusText != originalStatus
        val isCommentUnchangedOrEmpty = commentText.isEmpty() || commentText == originalComment

        if (isStatusChanged && isCommentUnchangedOrEmpty) {
            commentText = when (statusText) {
                "1" -> "Aprobado"
                "2" -> "Cupo lleno"
                else -> commentText
            }
        }

        val estadoMensaje = when (statusText) {
            "1" -> "aprobada"
            "2" -> "rechazada"
            else -> "en revisión"
        }

        newMessage =
            "Su solicitud realizada en $nameFormOperator para operador durante el $semesterFormOperator ha sido $estadoMensaje."

        val updateData = dataUpdateStatus(
            newStatusApplication = statusText.toInt(),
            newComment = commentText,
            userId = userId,
            message = newMessage
        )

        lifecycleScope.launch {
            try {
                if (statusText == "1") { // Aprobado
                    provider.operatorRegister(formStudentId)
                    toastMessage(
                        "Solicitud aprobada, operador registrado y rol actualizado",
                        ToastType.SUCCESS
                    )
                } else {
                    val updateSuccess =
                        provider.updateFormStatusAndComment(formStudentId, updateData)
                    if (updateSuccess) {
                        toastMessage("Datos actualizados correctamente", ToastType.SUCCESS)
                    } else {
                        toastMessage("Error al actualizar los datos", ToastType.ERROR)
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@DetailFormActivity, SolicitudesListView::class.java))
                    finish()
                }, 1000)
            } catch (e: Exception) {
                e.printStackTrace()
                toastMessage("Error: ${e.message}", ToastType.ERROR)
            }
        }

    }

    private fun finishActivityDetailsForm() {
        val backView = findViewById<ImageView>(R.id.backViewAdminDetailActivity)
        backView.setOnClickListener {
            val intent = Intent(this, SolicitudesListView::class.java)
            startActivity(intent)
            finish()
        }
    }

}
