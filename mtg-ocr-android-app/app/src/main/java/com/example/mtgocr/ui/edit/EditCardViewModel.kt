package com.example.mtgocr.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.domain.usecase.UpdateCardUseCase
import com.example.mtgocr.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCardViewModel(
    private val cardRepository: CardRepository,
    private val updateCardUseCase: UpdateCardUseCase
) : ViewModel() {

    private val _card = MutableStateFlow<CardDetails?>(null)
    val card: StateFlow<CardDetails?> = _card.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            try {
                _card.value = cardRepository.getById(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun save(updated: CardDetails, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            runCatching {
                updateCardUseCase(updated)
            }.onSuccess {
                onDone?.invoke()
            }.onFailure { t -> _error.value = t.message }
        }
    }
}
