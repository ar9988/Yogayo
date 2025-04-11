package com.d104.yogaapp.features.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.LoginResult
import com.d104.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginReducer: LoginReducer,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun processIntent(intent: LoginIntent) {
        val newState = loginReducer.reduce(_uiState.value, intent)
        _uiState.value = newState

        // 필요한 부수 효과 처리
        when (intent) {
            is LoginIntent.Login -> performLogin()
            else -> {} // 다른 Intent는 상태 업데이트만 필요하므로 추가 처리 없음
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            loginUseCase(uiState.value.id, uiState.value.password)
                .collect { result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)

                    result.fold(
                        onSuccess = { loginResult ->
                            when (loginResult) {
                                is LoginResult.Success -> {
                                    processIntent(LoginIntent.LoginSuccess)
                                }
                                is LoginResult.Error -> {
                                    val errorMessage = when (loginResult) {
                                        is LoginResult.Error.InvalidCredentials -> loginResult.message
                                        is LoginResult.Error.UserNotFound -> loginResult.message
                                    }
                                    processIntent(LoginIntent.LoginFailure(errorMessage))
                                }
                            }
                        },
                        onFailure = { throwable ->
                            processIntent(
                                LoginIntent.LoginFailure(
                                    throwable.message ?: "인터넷 접속 환경을 확인하세요."
                                )
                            )
                        }
                    )
                }
        }
    }
}