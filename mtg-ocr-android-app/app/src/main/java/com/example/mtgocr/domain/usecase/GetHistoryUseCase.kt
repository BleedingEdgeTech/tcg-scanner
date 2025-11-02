package com.example.mtgocr.domain.usecase

import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.repository.CardRepository
import kotlinx.coroutines.flow.Flow

class GetHistoryUseCase(private val cardRepository: CardRepository) {
    operator fun invoke(): Flow<List<CardDetails>> = cardRepository.observeHistory()
}