package com.d104.yogaapp.features.multi.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.usecase.CloseWebSocketUseCase
import com.d104.domain.usecase.ConnectWebSocketUseCase
import com.google.gson.JsonPrimitive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MultiPlayViewModel @Inject constructor(
    private val multiPlayReducer: MultiPlayReducer,
    private val connectWebSocketUseCase: ConnectWebSocketUseCase,
    private val closeWebSocketUseCase: CloseWebSocketUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MultiPlayState())
    val uiState: StateFlow<MultiPlayState> = _uiState.asStateFlow()

    fun processIntent(intent: MultiPlayIntent) {
        val newState = multiPlayReducer.reduce(_uiState.value, intent)
        _uiState.value = newState
        when(intent){
            is MultiPlayIntent.UpdateCameraPermission -> {
                if (intent.granted) {
                    // 카메라 권한 허용 시
                } else {
                    multiPlayReducer.reduce(_uiState.value, MultiPlayIntent.ExitRoom)
                }
            }
            else -> {}
        }
    }

    override fun onCleared() {
        CloseWebSocketUseCase()
        super.onCleared()
    }

    init {
        viewModelScope.launch {
            connectWebSocketUseCase()
            // userList Update
        }
    }
}