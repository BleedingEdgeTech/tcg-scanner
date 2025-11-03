package com.example.mtgocr.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.domain.usecase.CaptureCardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraUiState(
    val isProcessing: Boolean = false,
    val cardDetails: CardDetails? = null,
    val errorMessage: String? = null
)

class CameraViewModel(
    private val captureCardUseCase: CaptureCardUseCase,
    private val updateCardUseCase: com.example.mtgocr.domain.usecase.UpdateCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun processImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            val result = captureCardUseCase(imageBytes)

            _uiState.update {
                result.fold(
                    onSuccess = { card ->
                        CameraUiState(isProcessing = false, cardDetails = card)
                    },
                    onFailure = { throwable ->
                        it.copy(
                            isProcessing = false,
                            errorMessage = throwable.message ?: "Unable to recognize the card"
                        )
                    }
                )
            }
        }
    }

    fun dismissResult() {
        _uiState.update { CameraUiState() }
    }

    fun saveCard(card: CardDetails) {
        viewModelScope.launch {
            runCatching {
                updateCardUseCase(card)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    fun updateCard(updated: com.example.mtgocr.domain.model.CardDetails) {
        viewModelScope.launch {
            runCatching {
                updateCardUseCase(updated)
            }.onSuccess {
                _uiState.update { it.copy(cardDetails = updated) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    fun reportError(message: String) {
        _uiState.update { it.copy(isProcessing = false, errorMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}