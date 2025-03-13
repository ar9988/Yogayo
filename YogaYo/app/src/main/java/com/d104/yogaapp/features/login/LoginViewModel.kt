package com.d104.yogaapp.features.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            is LoginIntent.NavigateToSignUp -> {} // 네비게이션 처리는 UI에서 할 수도 있음
            else -> {} // 다른 Intent는 상태 업데이트만 필요하므로 추가 처리 없음
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            try {
                val result = loginUseCase(uiState.value.id, uiState.value.password)
                // 성공 시 성공 인텐트 발행
//                if(result.success)
                processIntent(LoginIntent.LoginSuccess)
//                else
                processIntent(LoginIntent.LoginFailure("로그인에 실패했습니다."))
            } catch (e: Exception) {
                // 실패 시 실패 인텐트 발행
                processIntent(LoginIntent.LoginFailure(e.message ?: "로그인에 실패했습니다."))
            }
        }
    }
}