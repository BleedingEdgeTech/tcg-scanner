package com.example.mtgocr.data.export

import android.content.Context
import android.os.Environment
import com.example.mtgocr.domain.model.CardDetails
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CsvExporter(private val context: Context) {

    fun export(cards: List<CardDetails>): File? {
        if (cards.isEmpty()) return null

        val fileName = "mtg_cards_export.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        return try {
            FileWriter(file).use { writer ->
                writer.appendLine("Name,Language,Collector Number,Set Code,Year of Print,Foil,Signed,Condition")
                cards.forEach { card ->
                    writer.appendLine(
                        listOf(
                            card.name,
                            card.language,
                            card.collectorNumber,
                            card.setCode,
                            card.yearOfPrint.toString(),
                            card.foil.toString(),
                            card.signed.toString(),
                            card.condition
                        ).joinToString(separator = ",") { value ->
                            value.replace(",", " ")
                        }
                    )
                }
            }
            file
        } catch (e: IOException) {
            null
        }
    }
}