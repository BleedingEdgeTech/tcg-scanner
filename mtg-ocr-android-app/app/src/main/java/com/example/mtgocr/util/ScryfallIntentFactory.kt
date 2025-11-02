package com.example.mtgocr.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object ScryfallIntentFactory {
    private const val SCRYFALL_BASE_URL = "https://scryfall.com"

    fun createCardSearchIntent(cardName: String): Intent {
        val searchUrl = "$SCRYFALL_BASE_URL/search?q=${Uri.encode(cardName)}"
        return Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}