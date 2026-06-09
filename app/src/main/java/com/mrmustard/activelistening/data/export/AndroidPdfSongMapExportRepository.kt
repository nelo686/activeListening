package com.mrmustard.activelistening.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.mrmustard.activelistening.domain.export.ExportSection
import com.mrmustard.activelistening.domain.export.SongMapExport
import com.mrmustard.activelistening.domain.export.SongMapExportRepository
import com.mrmustard.activelistening.domain.export.SongMapExportResult
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.time.formatTimeCode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPdfSongMapExportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SongMapExportRepository {

    override suspend fun exportPdf(
        destination: Uri,
        map: SongMapExport,
    ): SongMapExportResult = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(destination, "wt")?.use { output ->
                val document = createPdf(map)
                try {
                    document.writeTo(output)
                } finally {
                    document.close()
                }
            } ?: return@withContext SongMapExportResult.UnableToWrite
            SongMapExportResult.Success
        }.getOrDefault(SongMapExportResult.UnableToWrite)
    }

    private fun createPdf(map: SongMapExport): PdfDocument {
        val document = PdfDocument()
        val writer = PdfPageWriter(document)
        writer.writeTitle("Mapa estructural")
        writer.writeText(map.song.displayName, titlePaint)
        writer.writeText("Duracion: ${formatTimeCode(map.song.durationMillis)}", bodyPaint)
        writer.addSpace(18f)
        writer.writeText("Secciones", headingPaint)
        map.sections.forEach { section -> writer.writeSection(section) }
        writer.finish()
        return document
    }

    private class PdfPageWriter(
        private val document: PdfDocument,
    ) {
        private var pageNumber = 0
        private var page = newPage()
        private var canvas = page.canvas
        private var y = TOP_MARGIN

        fun writeTitle(text: String) {
            writeText(text, documentTitlePaint)
            addSpace(8f)
        }

        fun writeSection(exportSection: ExportSection) {
            val section = exportSection.section
            ensureSpace(150f)
            writeText(
                "${exportSection.index}. ${section.label.toDisplayName()} " +
                    "(${formatTimeCode(section.startMillis)} - ${formatTimeCode(section.endMillis)})",
                headingPaint,
            )
            writeText("Duracion: ${formatTimeCode(section.durationMillis)}", bodyPaint)
            writeText("Estado: ${section.status.toDisplayName()}", bodyPaint)
            val bars = section.rhythmInfo?.estimatedBars?.let { "$it compases estimados" }
                ?: "Compases no disponibles"
            writeText(bars, bodyPaint)
            if (section.isApproximate || section.status == SectionStatus.Uncertain) {
                writeText(
                    "Punto aproximado: revisa esta transicion escuchando la cancion.",
                    warningPaint,
                )
            }
            section.musicalContrast?.let { contrast ->
                writeWrappedText("Cambio ritmico: ${contrast.explanation}", bodyPaint)
            }
            writeWrappedText("Nota educativa: ${exportSection.educationalNote}", bodyPaint)
            addSpace(12f)
        }

        fun writeText(text: String, paint: Paint) {
            ensureSpace(paint.textSize + LINE_GAP)
            canvas.drawText(text, LEFT_MARGIN, y, paint)
            y += paint.textSize + LINE_GAP
        }

        fun writeWrappedText(text: String, paint: Paint) {
            text.wrap(MAX_LINE_CHARS).forEach { line -> writeText(line, paint) }
        }

        fun addSpace(space: Float) {
            ensureSpace(space)
            y += space
        }

        fun finish() {
            document.finishPage(page)
        }

        private fun ensureSpace(requiredHeight: Float) {
            if (y + requiredHeight <= PAGE_HEIGHT - BOTTOM_MARGIN) return
            document.finishPage(page)
            page = newPage()
            canvas = page.canvas
            y = TOP_MARGIN
        }

        private fun newPage(): PdfDocument.Page {
            pageNumber += 1
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            return document.startPage(pageInfo)
        }
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val LEFT_MARGIN = 48f
        const val TOP_MARGIN = 48f
        const val BOTTOM_MARGIN = 48f
        const val MAX_LINE_CHARS = 82
        const val LINE_GAP = 8f

        val documentTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
        }
        val warningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
    }
}

private fun String.wrap(maxChars: Int): List<String> {
    if (length <= maxChars) return listOf(this)
    val lines = mutableListOf<String>()
    var current = ""
    split(" ").forEach { word ->
        val candidate = if (current.isEmpty()) word else "$current $word"
        if (candidate.length <= maxChars) {
            current = candidate
        } else {
            if (current.isNotEmpty()) lines += current
            current = word
        }
    }
    if (current.isNotEmpty()) lines += current
    return lines
}

private fun SectionLabel.toDisplayName(): String =
    when (this) {
        SectionLabel.Intro -> "Intro"
        SectionLabel.Verse -> "Verso"
        SectionLabel.Chorus -> "Coro"
        SectionLabel.Bridge -> "Puente"
        SectionLabel.Outro -> "Outro"
        SectionLabel.Other -> "Otra"
    }

private fun SectionStatus.toDisplayName(): String =
    when (this) {
        SectionStatus.Suggested -> "Sugerida"
        SectionStatus.Confirmed -> "Confirmada"
        SectionStatus.Uncertain -> "Dudosa"
    }
