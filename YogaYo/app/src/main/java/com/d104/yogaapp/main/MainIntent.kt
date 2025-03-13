package com.d104.yogaapp.main

sealed class MainIntent {
    data class SelectTab(val tab: Tab) : MainIntent()
}