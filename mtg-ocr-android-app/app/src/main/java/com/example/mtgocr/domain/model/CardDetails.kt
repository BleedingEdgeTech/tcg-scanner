package com.example.mtgocr.domain.model

data class CardDetails(
    val id: Long? = null,
    val name: String,
    val language: String,
    val collectorNumber: String,
    val setCode: String,
    val setName: String,
    val yearOfPrint: Int,
    val cardMarketId: Int? = null,
    // mask fields
    val foil: Boolean = false,
    val signed: Boolean = false,
    val condition: String = "NM"
)