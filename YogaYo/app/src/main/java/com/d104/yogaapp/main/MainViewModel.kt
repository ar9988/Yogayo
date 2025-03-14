package com.d104.yogaapp.main

import androidx.lifecycle.ViewModel
import com.d104.domain.event.AuthEventManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val reducer: MainReducer,
    authEventManager: AuthEventManager,
) : ViewModel(){

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()
    val authEvents = authEventManager.authEvents

    fun processIntent(intent: MainIntent) {
        val newState = reducer.reduce(state.value, intent)
        _state.value = newState
    }
}