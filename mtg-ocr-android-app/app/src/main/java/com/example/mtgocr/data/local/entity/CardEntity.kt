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
    @ColumnInfo(name = "set_name") val setName: String,
    @ColumnInfo(name = "year_of_print") val yearOfPrint: Int,
    @ColumnInfo(name = "cardmarket_id") val cardMarketId: Int? = null,
    // mask fields
    val foil: Boolean = false,
    val signed: Boolean = false,
    val condition: String = "NM"
)

fun CardEntity.toDomain(): CardDetails = CardDetails(
    id = id,
    name = name,
    language = language,
    collectorNumber = collectorNumber,
    setCode = setCode,
    setName = setName,
    yearOfPrint = yearOfPrint,
    cardMarketId = cardMarketId,
    foil = foil,
    signed = signed,
    condition = condition
)

fun CardDetails.toEntity(): CardEntity = CardEntity(
    id = id ?: 0,
    name = name,
    language = language,
    collectorNumber = collectorNumber,
    setCode = setCode,
    setName = setName,
    yearOfPrint = yearOfPrint,
    cardMarketId = cardMarketId,
    foil = foil,
    signed = signed,
    condition = condition
)