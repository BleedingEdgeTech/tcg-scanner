package com.example.mtgocr.di

import com.example.mtgocr.data.export.CsvExporter
import com.example.mtgocr.data.local.CardDatabase
import com.example.mtgocr.data.remote.GeminiService
import com.example.mtgocr.domain.usecase.CaptureCardUseCase
import com.example.mtgocr.domain.usecase.UpdateCardUseCase
import com.example.mtgocr.domain.usecase.DeleteCardUseCase
import com.example.mtgocr.domain.usecase.ExportCardsUseCase
import com.example.mtgocr.domain.usecase.GetHistoryUseCase
import com.example.mtgocr.repository.CardRepository
import com.example.mtgocr.repository.GeminiRepository
import com.example.mtgocr.ui.camera.CameraViewModel
import com.example.mtgocr.ui.edit.EditCardViewModel
import com.example.mtgocr.ui.history.HistoryViewModel
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Gson() }
    single { CardDatabase.getInstance(androidContext()) }
    single { get<CardDatabase>().cardDao() }
    single { GeminiService.createApi() }
    single { CardRepository(get()) }
    single { GeminiRepository(get(), get()) }
    single { CsvExporter(androidContext()) }

    factory { CaptureCardUseCase(get(), get()) }
    factory { UpdateCardUseCase(get()) }
    factory { GetHistoryUseCase(get()) }
    factory { DeleteCardUseCase(get()) }
    factory { ExportCardsUseCase(get(), get()) }

    viewModel { CameraViewModel(get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { EditCardViewModel(get(), get()) }
}