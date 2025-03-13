package com.d104.yogaapp.features.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun processIntent(intent: SignUpIntent){
        val newState = signUpReducer.reduce(_uiState.value,intent)
        _uiState.value = newState

        when(intent) {
            is SignUpIntent.NavigateToLogin -> {}
            else -> {}
        }
    }

    private fun performSignUp(){
        viewModelScope.launch {
            try {
                val result = signUpUseCase(uiState.value.id,uiState.value.password)
//                if(result.success)
                processIntent(SignUpIntent.SignUpSuccess)
//                else
                processIntent(SignUpIntent.SignUpFailure("회원가입 실패"))
            }catch (e:Exception){
                processIntent(SignUpIntent.SignUpFailure(e.message?:"회원가입 실패"))
            }
        }
    }

}