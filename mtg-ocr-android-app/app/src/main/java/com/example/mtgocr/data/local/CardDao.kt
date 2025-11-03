package com.example.mtgocr.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mtgocr.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: CardEntity): Long

    @Query("SELECT * FROM cards ORDER BY id DESC")
    fun observeAll(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY id DESC")
    suspend fun getAllOnce(): List<CardEntity>

    @Query("DELETE FROM cards WHERE id = :cardId")
    suspend fun deleteById(cardId: Long)

    @Query("SELECT * FROM cards WHERE id = :cardId LIMIT 1")
    suspend fun getById(cardId: Long): CardEntity?

    @Query("DELETE FROM cards")
    suspend fun clearAll()

    @Query("SELECT * FROM cards WHERE collector_number = :collectorNumber LIMIT 1")
    suspend fun findByCollectorNumber(collectorNumber: String): CardEntity?
}