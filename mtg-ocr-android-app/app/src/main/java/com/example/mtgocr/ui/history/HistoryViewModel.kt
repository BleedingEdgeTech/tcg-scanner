package com.example.mtgocr.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.domain.usecase.DeleteCardUseCase
import com.example.mtgocr.domain.usecase.ExportCardsUseCase
import com.example.mtgocr.domain.usecase.GetHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteCardUseCase: DeleteCardUseCase,
    private val exportCardsUseCase: ExportCardsUseCase
) : ViewModel() {

    private val _history = MutableStateFlow<List<CardDetails>>(emptyList())
    val history: StateFlow<List<CardDetails>> = _history.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportPath = MutableStateFlow<String?>(null)
    val exportPath: StateFlow<String?> = _exportPath.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            getHistoryUseCase().collect { cards ->
                _history.value = cards
            }
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            deleteCardUseCase(cardId)
        }
    }

    fun exportHistory() {
        viewModelScope.launch {
            _isExporting.value = true
            val result = exportCardsUseCase()
            result.onSuccess { path ->
                _exportPath.value = path
            }.onFailure { throwable ->
                _errorMessage.value = throwable.message
            }
            _isExporting.value = false
        }
    }

    fun clearMessage() {
        _errorMessage.value = null
        _exportPath.value = null
    }
}