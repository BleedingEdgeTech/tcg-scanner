package com.example.mtgocr.data.remote

import com.example.mtgocr.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val urlWithKey = original.url.newBuilder()
            .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
            .build()
        val request = original.newBuilder().url(urlWithKey).build()
        chain.proceed(request)
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createApi(): GeminiApi = retrofit.create(GeminiApi::class.java)
}