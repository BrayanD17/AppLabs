package com.labs.applabs.export

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.ScheduleItem
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExportSchedulesActivity : AppCompatActivity() {

    private val provider = Provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_approved_schedules)

        val btnSiguiente = findViewById<Button>(R.id.btnExportExcel)
        val etEmail = findViewById<EditText>(R.id.etCorreoDestino)
        val messageLayout = findViewById<LinearLayout>(R.id.messageSuccess)
        val tvCorreoConfirmado = findViewById<TextView>(R.id.tvCorreoConfirmado)

        btnSiguiente.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val emailRegex = "^[a-zA-Z0-9._%+-]+@(itcr\\.ac\\.cr|estudiantec\\.cr)$".toRegex()

            if (!email.matches(emailRegex)) {
                etEmail.error = "El correo debe ser @itcr.ac.cr o @estudiantec.cr"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val solicitudes = provider.getSolicitudes()
                val solicitudesAprobadas = solicitudes.mapNotNull { solicitud ->
                    val form = provider.getFormStudent(solicitud.idFormStudent)
                    form?.let {
                        if (it.studentInfo.statusApplication == "1") {
                            Pair("${it.studentInfo.studentName} ${it.studentInfo.surNames}", it.studentInfo.scheduleAvailability)
                        } else null
                    }
                }

                val file = generateExcelFile(solicitudesAprobadas)

                tvCorreoConfirmado.text = "Seleccioná Outlook (u otra app) para enviar el archivo a $email"
                messageLayout.visibility = View.VISIBLE

                abrirAppDeCorreo(email, file)
            }
        }
    }

    private fun generateExcelFile(solicitudes: List<Pair<String, List<ScheduleItem>>>): File {
        val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val horas = listOf("7 a 12", "12 a 5", "5 a 10")

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Horarios Aprobados")

        val boldFont = workbook.createFont().apply {
            bold = true
        }

        val headerStyle = workbook.createCellStyle().apply {
            setFont(boldFont)
            alignment = HorizontalAlignment.CENTER
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val checkStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val defaultStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).apply {
            setCellValue("Nombre completo")
            cellStyle = headerStyle
        }

        var colIndex = 1
        for (dia in dias) {
            for (hora in horas) {
                headerRow.createCell(colIndex++).apply {
                    setCellValue("$dia $hora")
                    cellStyle = headerStyle
                }
            }
        }

        for ((rowIndex, pair) in solicitudes.withIndex()) {
            val (nombre, disponibilidad) = pair
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).apply {
                setCellValue(nombre)
                cellStyle = defaultStyle
            }

            val mapa = disponibilidad.associate { it.day to it.shift.toSet() }

            colIndex = 1
            for (dia in dias) {
                for (hora in horas) {
                    val cell = row.createCell(colIndex++)
                    if (mapa[dia]?.contains(hora) == true) {
                        cell.setCellValue("✔")
                        cell.cellStyle = checkStyle
                    } else {
                        cell.cellStyle = defaultStyle
                    }
                }
            }
        }

        for (i in 0..(dias.size * horas.size)) {
            sheet.autoSizeColumn(i)
        }

        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Horarios_Aprobados.xlsx")
        FileOutputStream(file).use {
            workbook.write(it)
            workbook.close()
        }

        return file
    }

    private fun abrirAppDeCorreo(destinatario: String, archivo: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            archivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
            putExtra(Intent.EXTRA_SUBJECT, "Horarios Aprobados")
            putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás el archivo Excel con los horarios aprobados.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Selecciona Outlook o tu app de correo"))
    }
}
