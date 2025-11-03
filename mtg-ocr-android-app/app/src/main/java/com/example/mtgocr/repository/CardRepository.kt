package com.example.mtgocr.repository

import com.example.mtgocr.data.local.CardDao
import com.example.mtgocr.data.local.entity.toDomain
import com.example.mtgocr.data.local.entity.toEntity
import com.example.mtgocr.domain.model.CardDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardRepository(private val cardDao: CardDao) {

    fun observeHistory(): Flow<List<CardDetails>> =
        cardDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun getHistorySnapshot(): List<CardDetails> =
        cardDao.getAllOnce().map { it.toDomain() }

    suspend fun getById(id: Long): CardDetails? =
        cardDao.getById(id)?.toDomain()

    suspend fun saveCard(card: CardDetails): Long =
        cardDao.upsert(card.toEntity())

    suspend fun removeCard(cardId: Long) {
        cardDao.deleteById(cardId)
    }

    suspend fun clearHistory() {
        cardDao.clearAll()
    }

    suspend fun getByCollectorNumber(collectorNumber: String): CardDetails? =
        cardDao.findByCollectorNumber(collectorNumber)?.toDomain()
}