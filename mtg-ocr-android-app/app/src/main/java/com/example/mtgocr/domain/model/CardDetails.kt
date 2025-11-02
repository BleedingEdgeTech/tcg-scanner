package com.example.mtgocr.domain.model

data class CardDetails(
    val id: Long? = null,
    val name: String,
    val language: String,
    val collectorNumber: String,
    val setCode: String,
    val yearOfPrint: Int
)