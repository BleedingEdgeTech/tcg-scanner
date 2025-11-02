package com.example.mtgocr.domain.usecase

import com.example.mtgocr.repository.CardRepository

class DeleteCardUseCase(private val cardRepository: CardRepository) {

    suspend operator fun invoke(cardId: Long) {
        cardRepository.removeCard(cardId)
    }
}