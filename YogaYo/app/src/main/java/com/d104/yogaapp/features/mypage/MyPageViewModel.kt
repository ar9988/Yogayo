package com.d104.yogaapp.features.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d104.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val myPageReducer: MyPageReducer,
    private val logoutUseCase: LogoutUseCase
): ViewModel(){
    private val _uiState = MutableStateFlow(MyPageState())
    val uiState : StateFlow<MyPageState> = _uiState.asStateFlow()

    fun processIntent(intent: MyPageIntent){
        val newState = myPageReducer.reduce(_uiState.value,intent)
        _uiState.value = newState
        when(intent){
            is MyPageIntent.Logout -> {
                performLogout()
            }
            is MyPageIntent.LogoutSuccess -> {

            }
        }
    }

    private fun performLogout(){
        _uiState.value = _uiState.value.copy(isLoading = true)
        processIntent(MyPageIntent.LogoutSuccess)
        viewModelScope.launch{

            logoutUseCase()
                .collect{ result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if(result){
                        processIntent(MyPageIntent.LogoutSuccess)
                    }
                }
        }
    }
}