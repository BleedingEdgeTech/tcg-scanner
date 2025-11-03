package com.example.mtgocr.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object ScryfallIntentFactory {
    private const val SCRYFALL_BASE_URL = "https://scryfall.com"

    /**
     * Erzeugt ein Intent, das auf die Scryfall-Seite für eine bestimmte Karte zeigt.
     * Versucht zuerst, eine direkte Card-URL mit `setCode` und `collectorNumber` zu bauen.
     * Falls das nicht verwendet werden kann (leere Werte), fällt es auf eine Suche zurück,
     * die den Kartennamen und optional das Jahr der Erstveröffentlichung verwendet.
     *
     * Gründe: In einigen Fällen stimmen `setCode`+`collectorNumber` nicht mit Scryfalls URL-Schema überein,
     * daher ist ein Fallback auf `name + year` zuverlässiger.
     */
    fun createCardIntent(
        cardName: String,
        setCode: String? = null,
        collectorNumber: String? = null,
        year: String? = null
    ): Intent {
        val targetUrl = buildCardUrl(setCode, collectorNumber, cardName, year)
        return Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun buildCardUrl(
        setCode: String?,
        collectorNumber: String?,
        cardName: String,
        year: String?
    ): String {
        // Wenn setCode und collectorNumber vorhanden sind, versuche die direkte Card-URL
        if (!setCode.isNullOrBlank() && !collectorNumber.isNullOrBlank()) {
            // Scryfall card URLs sind üblicherweise: /card/{set}/{number}/{name-slug}
            // Wir bauen die URL robust mit Uri.encode für den Namen.
            val safeSet = setCode.trim().lowercase()
            val safeNumber = collectorNumber.trim()
            val nameSlug = Uri.encode(cardName.trim().lowercase().replace(" ", "-"))
            return "$SCRYFALL_BASE_URL/card/$safeSet/$safeNumber/$nameSlug"
        }

        // Fallback: Suche nach Name + Jahr (wenn Jahr vorhanden), sonst nur nach Name
        val query = if (!year.isNullOrBlank()) {
            "${cardName.trim()} ${year.trim()}"
        } else {
            cardName.trim()
        }
        return "$SCRYFALL_BASE_URL/search?q=${Uri.encode(query)}"
    }
}