package com.example.mtgocr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mtgocr.domain.model.CardDetails

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val language: String,
    @ColumnInfo(name = "collector_number") val collectorNumber: String,
    @ColumnInfo(name = "set_code") val setCode: String,
    @ColumnInfo(name = "year_of_print") val yearOfPrint: Int
)

fun CardEntity.toDomain(): CardDetails = CardDetails(
    id = id,
    name = name,
    language = language,
    collectorNumber = collectorNumber,
    setCode = setCode,
    yearOfPrint = yearOfPrint
)

fun CardDetails.toEntity(): CardEntity = CardEntity(
    id = id ?: 0,
    name = name,
    language = language,
    collectorNumber = collectorNumber,
    setCode = setCode,
    yearOfPrint = yearOfPrint
)