package com.labs.applabs.administrator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.R
import com.labs.applabs.firebase.AssignedScheduleData
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream

class AdminGeneralScheduleActivity : AppCompatActivity() {

    // --- Para mostrar tabla desde Provider ---
    private lateinit var tableGeneralSchedule: TableLayout
    private val provider = Provider()
    private var listaHorariosGenerales: List<AssignedScheduleData> = emptyList()

    // --- Para exportar Excel desde Firestore ---
    private lateinit var btnExportExcel: Button
    private val db = FirebaseFirestore.getInstance()

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        if (uri != null) {
            lifecycleScope.launchWhenStarted {
                exportarHorariosAsignadosAExcel(uri)
            }
        } else {
            Toast.makeText(this, "Exportación cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_general_schedule)

        // --- Lógica para mostrar el horario en la app ---
        tableGeneralSchedule = findViewById(R.id.tableGeneralSchedule)
        lifecycleScope.launch {
            // Traer de tu provider la lista, convertir a AssignedScheduleData
            val originales = provider.obtenerHorariosAsignadosGeneral()
            listaHorariosGenerales = originales.map {
                AssignedScheduleData(
                    name = "",
                    laboratory = it.laboratory,
                    shift = it.shift,
                    day = it.day,
                    operator = it.operator,
                    scheduleMatrix = emptyMap()
                )
            }
            mostrarHorarioGeneral(listaHorariosGenerales)
        }

        // --- Lógica para exportar Excel con tu lógica original ---
        btnExportExcel = findViewById(R.id.btnExportGeneralSchedule)
        btnExportExcel.setOnClickListener {
            createDocumentLauncher.launch("HorariosAsignados.xlsx")
        }
    }

    // ----------- TABLA VISUAL -----------
    private fun mostrarHorarioGeneral(listaHorarios: List<AssignedScheduleData>) {
        val laboratorios = listaHorarios.map { it.laboratory }.distinct()
        val dias = listOf("L", "K", "M", "J", "V", "S")
        val diasMap = mapOf(
            "Lunes" to "L", "Martes" to "K", "Miércoles" to "M",
            "Jueves" to "J", "Viernes" to "V", "Sábado" to "S"
        )
        val turnos = listOf("7 a 12", "12 a 5", "5 a 10")

        val tabla =
            mutableMapOf<String, MutableMap<String, MutableMap<String, MutableList<String>>>>()
        for (lab in laboratorios) {
            tabla[lab] = mutableMapOf()
            for (turno in turnos) {
                tabla[lab]!![turno] = mutableMapOf()
                for (dia in dias) {
                    tabla[lab]!![turno]!![dia] = mutableListOf()
                }
            }
        }
        for (h in listaHorarios) {
            val lab = h.laboratory
            val turno = h.shift
            val dia = diasMap[h.day] ?: h.day
            tabla[lab]?.get(turno)?.get(dia)?.add(h.operator)
        }

        tableGeneralSchedule.removeAllViews()

        // Encabezado
        val headerRow = TableRow(this)
        headerRow.addView(
            celdaHorario(
                "",
                bold = true,
                fondo = 0xFF1565C0.toInt(),
                colorTexto = 0xFFFFFFFF.toInt()
            )
        )
        headerRow.addView(
            celdaHorario(
                "",
                bold = true,
                fondo = 0xFF222222.toInt(),
                colorTexto = 0xFFFFFFFF.toInt()
            )
        )
        for (dia in dias) headerRow.addView(
            celdaHorario(
                dia,
                bold = true,
                fondo = 0xFF43A047.toInt(),
                colorTexto = 0xFFFFFFFF.toInt()
            )
        )
        tableGeneralSchedule.addView(headerRow)

        // Filas por laboratorio
        for (lab in laboratorios) {
            val labRow = TableRow(this)
            val labCell = celdaHorario(
                lab,
                bold = true,
                fondo = 0xFF1565C0.toInt(),
                colorTexto = 0xFFFFFFFF.toInt()
            )
            val lp = TableRow.LayoutParams()
            lp.span = dias.size + 2
            labCell.layoutParams = lp
            labRow.addView(labCell)
            tableGeneralSchedule.addView(labRow)

            for (turno in turnos) {
                val row = TableRow(this)
                row.addView(celdaHorario("", fondo = 0xFF1565C0.toInt()))
                row.addView(
                    celdaHorario(
                        turno,
                        bold = true,
                        fondo = 0xFF222222.toInt(),
                        colorTexto = 0xFFFFFFFF.toInt()
                    )
                )
                for (dia in dias) {
                    val listaOp = tabla[lab]?.get(turno)?.get(dia) ?: emptyList()
                    val texto = listaOp.joinToString("\n")
                    val celda = if (texto.isBlank()) {
                        celdaHorario("", fondo = 0xFFFFFFFF.toInt())
                    } else {
                        celdaHorario(texto, fondo = 0xFFE3F2FD.toInt())
                    }
                    row.addView(celda)
                }
                tableGeneralSchedule.addView(row)
            }
        }
    }

    private fun celdaHorario(
        text: String,
        bold: Boolean = false,
        fondo: Int? = null,
        colorTexto: Int? = null
    ): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.setPadding(8, 8, 8, 8)
        tv.textSize = 13f
        tv.setBackgroundResource(R.drawable.celda_borde)
        tv.maxLines = 4
        tv.ellipsize = TextUtils.TruncateAt.END
        tv.setTextColor(colorTexto ?: 0xFF222222.toInt())
        if (bold) tv.setTypeface(null, Typeface.BOLD)
        fondo?.let { tv.setBackgroundColor(it) }
        return tv
    }

    // ----------- EXPORTAR EXCEL (LÓGICA ORIGINAL TUYA) -----------
    private suspend fun exportarHorariosAsignadosAExcel(uri: Uri) {
        try {
            btnExportExcel.isEnabled = false
            btnExportExcel.text = "Exportando..."

            val snapshot = db.collection("assignSchedule").get().await()
            val listaDatos = mutableListOf<AssignedScheduleData>()

            for (doc in snapshot.documents) {
                val userId = doc.getString("userId") ?: continue
                val userDoc = db.collection("users").document(userId).get().await()
                val nombre = userDoc.getString("name") ?: "Sin nombre"
                val apellidos = userDoc.getString("surnames") ?: ""
                val nombreCompleto = "$nombre $apellidos".trim()
                val labs = doc.get("labs") as? Map<*, *> ?: continue
                val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
                val turnos = listOf("Mañana", "Tarde", "Noche")

                for ((labName, daysMapAny) in labs) {
                    val daysMap = daysMapAny as? Map<*, *> ?: continue
                    val scheduleMatrix = mutableMapOf<String, List<String>>()
                    for (dia in dias) {
                        val turnosDia =
                            (daysMap[dia] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        scheduleMatrix[dia] = turnosDia
                    }
                    // Aquí los campos irrelevantes para Excel van vacíos
                    listaDatos.add(
                        AssignedScheduleData(
                            name = nombreCompleto,
                            laboratory = labName.toString(),
                            shift = "",
                            day = "",
                            operator = "",
                            scheduleMatrix = scheduleMatrix
                        )
                    )
                }
            }
            runOnUiThread {
                Toast.makeText(this, "¡Horario general exportado correctamente!", Toast.LENGTH_LONG).show()
                mostrarNotificacionExcel(uri)
            }

            if (listaDatos.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "No hay horarios asignados para exportar.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                btnExportExcel.isEnabled = true
                btnExportExcel.text = "Descargar archivo Excel"
                return
            }

            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.use { output ->
                    crearExcelPorLaboratorio(listaDatos, output)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            runOnUiThread {
                btnExportExcel.isEnabled = true
                btnExportExcel.text = "Descargar archivo Excel"
            }
        }
    }
    private fun mostrarNotificacionExcel(uri: Uri) {
        // El nombre del archivo
        val fileName = "HorarioGeneralOperadores.xlsx"
        val channelId = "descarga_excel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un Intent para abrir el archivo Excel
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Canal de notificación para Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Descargas de Excel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Notificación
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_download_24) // Usa tu icono de Excel o genérico
            .setContentTitle("Excel descargado")
            .setContentText("¡El archivo $fileName está listo! Toca para abrirlo.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(1001, builder.build())
    }

    private fun crearExcelPorLaboratorio(
        lista: List<AssignedScheduleData>, output: OutputStream
    ) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Horario General")
        val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val diasCorto = listOf("L", "K", "M", "J", "V", "S")
        val turnos = listOf("Mañana", "Tarde", "Noche")
        val labs = lista.map { it.laboratory }.distinct()

        val tabla =
            mutableMapOf<String, MutableMap<String, MutableMap<String, MutableList<String>>>>()
        for (lab in labs) {
            tabla[lab] = mutableMapOf()
            for (turno in turnos) {
                tabla[lab]!![turno] = mutableMapOf()
                for (dia in dias) {
                    tabla[lab]!![turno]!![dia] = mutableListOf()
                }
            }
        }
        for (dato in lista) {
            val lab = dato.laboratory
            val scheduleMatrix = dato.scheduleMatrix
            for ((dia, turnosDia) in scheduleMatrix) {
                for (turno in turnosDia) {
                    if (turno in turnos && dia in dias)
                        tabla[lab]?.get(turno)?.get(dia)?.add(dato.name)
                }
            }
        }

        var rowIdx = 0

        // Estilos
        val labStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
            })
            alignment = HorizontalAlignment.CENTER
        }
        val dayStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
            })
            alignment = HorizontalAlignment.CENTER
        }
        val turnoStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.BLACK.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
            })
            alignment = HorizontalAlignment.CENTER
        }
        val nombreStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.PALE_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            setFont(workbook.createFont().apply { bold = true })
            wrapText = true
        }
        val vacioStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
        }

        for (lab in labs) {
            val labRow = sheet.createRow(rowIdx++)
            labRow.createCell(0).apply { setCellValue(lab); cellStyle = labStyle }
            for (i in 1..dias.size) labRow.createCell(i).cellStyle = labStyle
            sheet.addMergedRegion(
                org.apache.poi.ss.util.CellRangeAddress(
                    labRow.rowNum,
                    labRow.rowNum,
                    0,
                    dias.size
                )
            )

            val diasRow = sheet.createRow(rowIdx++)
            diasRow.createCell(0)
            for ((i, dC) in diasCorto.withIndex()) {
                diasRow.createCell(i + 1).apply { setCellValue(dC); cellStyle = dayStyle }
            }

            for (turno in turnos) {
                val row = sheet.createRow(rowIdx++)
                row.createCell(0).apply { setCellValue(turno); cellStyle = turnoStyle }
                for ((dIdx, dia) in dias.withIndex()) {
                    val nombres = tabla[lab]?.get(turno)?.get(dia) ?: emptyList()
                    val celda = row.createCell(dIdx + 1)
                    if (nombres.isNotEmpty()) {
                        celda.setCellValue(nombres.joinToString("\n"))
                        celda.cellStyle = nombreStyle
                    } else {
                        celda.setCellValue("")
                        celda.cellStyle = vacioStyle
                    }
                }
            }
            rowIdx++
        }
        for (i in 0..dias.size) sheet.setColumnWidth(i, 20 * 256)
        workbook.write(output)
        workbook.close()
    }

}
