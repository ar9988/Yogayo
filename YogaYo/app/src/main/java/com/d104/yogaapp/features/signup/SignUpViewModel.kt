package com.d104.yogaapp.features.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.SignUpResult
import com.d104.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpReducer: SignUpReducer,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()

    fun processIntent(intent: SignUpIntent) {
        val newState = signUpReducer.reduce(_uiState.value, intent)
        _uiState.value = newState

        when (intent) {
            is SignUpIntent.SignUp -> {
                performSignUp()
            }

            else -> {}
        }
    }

    private fun performSignUp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            signUpUseCase(
                uiState.value.id,
                uiState.value.password,
                uiState.value.name,
                uiState.value.nickname,
                uiState.value.uri
            ).collect { result ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                result.fold(
                    onSuccess = { signUpResult ->
                        when (signUpResult) {
                            is SignUpResult.Success -> {
                                processIntent(SignUpIntent.SignUpSuccess)
                            }

                            is SignUpResult.Error -> {
                                val errorMessage = when (signUpResult) {
                                    is SignUpResult.Error.BadRequest -> signUpResult.message
                                    is SignUpResult.Error.ConflictUser -> signUpResult.message
                                }
                                processIntent(SignUpIntent.SignUpFailure(errorMessage))
                            }
                        }
                    },
                    onFailure = { throwable ->
                        processIntent(
                            SignUpIntent.SignUpFailure(
                                throwable.message ?: "네트워크 접속 환경을 확인해 주세요."
                            )
                        )
                    }
                )
            }
        }
    }
}