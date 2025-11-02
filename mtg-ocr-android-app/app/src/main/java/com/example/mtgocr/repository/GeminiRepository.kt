package com.example.mtgocr.repository

import android.util.Base64
import com.example.mtgocr.data.remote.GeminiApi
import com.example.mtgocr.data.remote.GeminiContent
import com.example.mtgocr.data.remote.GeminiInlineData
import com.example.mtgocr.data.remote.GeminiPart
import com.example.mtgocr.data.remote.GeminiRequest
import com.example.mtgocr.domain.model.CardDetails
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

private const val CARD_PROMPT = """
You are assisting with cataloging Magic: The Gathering cards. Analyze the provided card photo and respond with a single JSON object containing these keys: name, language, collectorNumber, setCode, yearOfPrint. The year must be the four digit printing year. If any value is unknown, set it to an empty string. Only return JSON without additional text.
"""

class GeminiRepository(
	private val geminiApi: GeminiApi,
	private val gson: Gson
) {

	suspend fun recognizeCard(imageBytes: ByteArray): CardDetails = withContext(Dispatchers.IO) {
		val request = GeminiRequest(
			contents = listOf(
				GeminiContent(
                    role = "user",
					parts = listOf(
						GeminiPart(text = CARD_PROMPT.trimIndent()),
						GeminiPart(
							inline_data = GeminiInlineData(
								mime_type = "image/jpeg",
								data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
							)
						)
					)
				)
			)
		)

        try {
            val response = geminiApi.analyzeCard(request)
            val rawText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()
                ?: throw IOException("Failed to parse Gemini response. The response or its parts were empty.")

            parseCardDetails(rawText)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            throw IOException("API Error: ${e.code()} - ${e.message()}. Body: $errorBody", e)
        } catch (e: Exception) {
            throw IOException("An unexpected error occurred: ${e.message}", e)
        }
	}

	private fun parseCardDetails(text: String): CardDetails {
		val sanitized = text
			.replace("```json", "", ignoreCase = true)
			.replace("```", "")
			.trim()

		val jsonElement: JsonElement = gson.fromJson(sanitized, JsonElement::class.java)
		val jsonObject = jsonElement.asJsonObject

		return CardDetails(
			name = jsonObject.get("name")?.asString.orEmpty(),
			language = jsonObject.get("language")?.asString.orEmpty(),
			collectorNumber = jsonObject.get("collectorNumber")?.asString.orEmpty(),
			setCode = jsonObject.get("setCode")?.asString.orEmpty(),
			yearOfPrint = jsonObject.get("yearOfPrint")?.let { element ->
				when {
					element.isJsonNull -> 0
					element.asString.isNullOrBlank() -> 0
					element.asString.matches(Regex("\\d{4}")) -> element.asString.toInt()
					element.asJsonPrimitive.isNumber -> element.asInt
					else -> 0
				}
			} ?: 0
		)
	}
}
