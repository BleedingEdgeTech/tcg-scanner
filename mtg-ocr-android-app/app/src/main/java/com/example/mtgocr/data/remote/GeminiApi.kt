package com.example.mtgocr.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface GeminiApi {
        @POST("v1beta/models/gemini-flash-lite-latest:generateContent")
    suspend fun analyzeCard(@Body request: GeminiRequest): GeminiResponse
}

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiPart(
    val text: String? = null,
    val inline_data: GeminiInlineData? = null
)

data class GeminiInlineData(
    val mime_type: String,
    val data: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)