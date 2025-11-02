package com.example.mtgocr.domain.usecase

import com.example.mtgocr.data.export.CsvExporter
import com.example.mtgocr.repository.CardRepository

class ExportCardsUseCase(
    private val cardRepository: CardRepository,
    private val csvExporter: CsvExporter
) {

    suspend operator fun invoke(): Result<String> = runCatching {
        val cards = cardRepository.getHistorySnapshot()
        val exportedFile = csvExporter.export(cards)
            ?: throw IllegalStateException("Failed to create CSV file")
        exportedFile.absolutePath
    }
}