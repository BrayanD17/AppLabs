package com.labs.applabs.export

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.labs.applabs.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream

class ExportSchedulesActivity : AppCompatActivity() {

    private lateinit var btnExportExcel: Button
    private val db = FirebaseFirestore.getInstance()

    // SAF: Lanzador para elegir carpeta y crear archivo
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri: Uri? ->
        if (uri != null) {
            lifecycleScope.launchWhenStarted {
                exportarHorariosAExcel(uri)
            }
        } else {
            Toast.makeText(this, "Exportación cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_approved_schedules)
        btnExportExcel = findViewById(R.id.btnExportExcel)

        btnExportExcel.setOnClickListener {
            // Abrir selector de ubicación para guardar archivo
            createDocumentLauncher.launch("HorariosOperadores.xlsx")
        }
    }

    private suspend fun exportarHorariosAExcel(uri: Uri) {
        try {
            btnExportExcel.isEnabled = false
            btnExportExcel.text = "Exportando..."

            // 1. Obtener historial de operadores
            val historialSnap = db.collection("historialOperadores").get().await()
            val historial = historialSnap.documents

            //Probando
            runOnUiThread {
                Toast.makeText(this, "Historial size: ${historial.size}", Toast.LENGTH_LONG).show()
            }
            android.util.Log.d("EXPORT", "Historial size: ${historial.size}")

            // 2. Preparar estructura para horarios
            val listaDatos = mutableListOf<DatosHorarioOperador>()
            for (doc in historial) {
                val userId = doc.getString("userId") ?: continue
                val nombre = doc.getString("nombreUsuario") ?: continue
                val formId = doc.getString("formId") ?: continue
                val formSnap = db.collection("formStudent").document(formId).get().await()
                //Probando
                android.util.Log.d("EXPORT", "Procesando: $nombre, userId=$userId, formId=$formId")

                if (!formSnap.exists()) continue
                val form = formSnap.data ?: continue

                val horas = form["shift"]?.toString() ?: ""
                val horariosList = (form["scheduleAvailability"] as? List<Map<String, Any>>)
                    ?: (form["scheduleAvailability"] as? List<HashMap<String, Any>>)
                    ?: emptyList()

                val horarios = horariosList.mapNotNull { diaMap ->
                    val day = diaMap["day"] as? String ?: return@mapNotNull null
                    val shifts = diaMap["shifts"] as? List<String> ?: emptyList()
                    ScheduleItem(day, shifts)
                }
                listaDatos.add(DatosHorarioOperador(horas, nombre, horarios))
            }

            if (listaDatos.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this, "No hay operadores aprobados para exportar.", Toast.LENGTH_LONG).show()
                }
                btnExportExcel.isEnabled = true
                btnExportExcel.text = "Descargar archivo Excel"
                return
            }

            // 3. Crear y guardar el Excel usando SAF
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.use { output ->
                    crearExcelConHorarios(listaDatos, output)
                }
            }

            runOnUiThread {
                Toast.makeText(this, "¡Excel guardado correctamente!", Toast.LENGTH_LONG).show()
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

    // ---- Formato del Excel ----

    private fun crearExcelConHorarios(lista: List<DatosHorarioOperador>, output: OutputStream) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Horarios Operadores")

        val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val turnos = listOf("7am a 12pm", "12pm a 5pm", "5pm a 10pm")

        // Colores para los días
        val colorDias = mapOf(
            "Lunes" to IndexedColors.LIGHT_YELLOW.index,
            "Martes" to IndexedColors.LIGHT_GREEN.index,
            "Miércoles" to IndexedColors.LIGHT_CORNFLOWER_BLUE.index,
            "Jueves" to IndexedColors.LIGHT_ORANGE.index,
            "Viernes" to IndexedColors.TAN.index,
            "Sábado" to IndexedColors.LIGHT_TURQUOISE.index,
            "Domingo" to IndexedColors.LIGHT_BLUE.index,
        )

        // Estilo de encabezado negro
        val fontWhiteBold = workbook.createFont().apply {
            color = IndexedColors.WHITE.index
            bold = true
        }
        val styleBlackHeader = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.BLACK.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            setFont(fontWhiteBold)
        }

        // Estilo para columnas Horas/Nombres
        val styleFijo = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        // Estilo general de celda por día
        fun estiloCelda(color: Short) = workbook.createCellStyle().apply {
            fillForegroundColor = color
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        // Encabezado fila 0 (días)
        val headerDias = sheet.createRow(0)
        headerDias.createCell(0).apply { setCellValue("Horas"); cellStyle = styleBlackHeader }
        headerDias.createCell(1).apply { setCellValue("Nombres"); cellStyle = styleBlackHeader }
        var col = 2
        for (dia in dias) {
            // Combinar celdas para el día (ocupa 3 columnas)
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, col, col + 2))
            val cell = headerDias.createCell(col)
            cell.setCellValue(dia)
            cell.cellStyle = styleBlackHeader
            // Siguientes 2 celdas solo para que tengan el mismo fondo
            for (k in 1..2) {
                headerDias.createCell(col + k).cellStyle = styleBlackHeader
            }
            col += 3
        }

        // Encabezado fila 1 (turnos)
        val headerTurnos = sheet.createRow(1)
        headerTurnos.createCell(0).apply { setCellValue(""); cellStyle = styleBlackHeader }
        headerTurnos.createCell(1).apply { setCellValue(""); cellStyle = styleBlackHeader }
        col = 2
        for (dia in dias) {
            for (turno in turnos) {
                headerTurnos.createCell(col).apply {
                    setCellValue(turno)
                    cellStyle = styleBlackHeader
                }
                col++
            }
        }

        // Llenar filas a partir de la 2
        lista.forEachIndexed { i, op ->
            val row = sheet.createRow(i + 2)
            row.createCell(0).apply { setCellValue(op.horas); cellStyle = styleFijo }
            row.createCell(1).apply { setCellValue(op.nombre); cellStyle = styleFijo }
            col = 2
            for (dia in dias) {
                for (turno in turnos) {
                    val tieneTurno = op.horarios.any { it.day.equals(dia, true) && it.shifts.contains(turno) }
                    val cell = row.createCell(col)
                    cell.setCellValue(if (tieneTurno) "✔" else "□")
                    cell.cellStyle = estiloCelda(colorDias[dia] ?: IndexedColors.WHITE.index)
                    col++
                }
            }
        }

        // Ajustar ancho de columnas
        for (i in 0 until dias.size * turnos.size + 2) {
            sheet.setColumnWidth(i, 16 * 256)
        }

        workbook.write(output)
        workbook.close()
    }



    // ---- Modelos auxiliares ----

    data class DatosHorarioOperador(
        val horas: String,
        val nombre: String,
        val horarios: List<ScheduleItem>
    )

    data class ScheduleItem(
        val day: String,
        val shifts: List<String>
    )
}
