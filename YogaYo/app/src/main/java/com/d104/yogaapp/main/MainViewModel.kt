package com.d104.yogaapp.main

import androidx.lifecycle.ViewModel
import com.d104.domain.event.AuthEventManager
import com.d104.domain.usecase.GetLoginStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val reducer: MainReducer,
    authEventManager: AuthEventManager,
    private val getLoginStatusUseCase: GetLoginStatusUseCase
) : ViewModel(){

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()
    val authEvents = authEventManager.authEvents

    fun processIntent(intent: MainIntent) {
        val newState = reducer.reduce(state.value, intent)
        _state.value = newState
        when (intent) {
            is MainIntent.SelectTab -> {
                // 로그인이 필요한 탭인지 확인
                if ((intent.tab == Tab.Multi || intent.tab == Tab.MyPage) && !isLoggedIn()) {
                    processIntent(MainIntent.SelectTab(Tab.Login))
                }
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return runBlocking { getLoginStatusUseCase().first() }
    }
}