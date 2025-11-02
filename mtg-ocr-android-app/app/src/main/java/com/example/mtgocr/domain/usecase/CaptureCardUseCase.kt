package com.example.mtgocr.domain.usecase

import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.repository.CardRepository
import com.example.mtgocr.repository.GeminiRepository

class CaptureCardUseCase(
    private val geminiRepository: GeminiRepository,
    private val cardRepository: CardRepository
) {

    suspend operator fun invoke(imageBytes: ByteArray): Result<CardDetails> = runCatching {
        val cardDetails = geminiRepository.recognizeCard(imageBytes)
        val id = cardRepository.saveCard(cardDetails)
        cardDetails.copy(id = id)
    }
}