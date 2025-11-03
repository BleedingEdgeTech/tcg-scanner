package com.example.mtgocr.domain.usecase

import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.repository.CardRepository

class UpdateCardUseCase(private val cardRepository: CardRepository) {
    suspend operator fun invoke(card: CardDetails): Result<Long> = runCatching {
        cardRepository.saveCard(card)
    }
}
