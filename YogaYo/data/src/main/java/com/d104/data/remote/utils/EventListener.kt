package com.d104.data.remote.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import javax.inject.Inject

class EventListener @Inject constructor(): EventSourceListener() {
    private val _sseEvents = MutableStateFlow<String>("")
    val sseEvents: StateFlow<String> = _sseEvents
    override fun onOpen(eventSource: EventSource, response: Response) {
        println("SSE Connection Opened")
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        println("Event Data: $data")
        _sseEvents.value = data
    }

    override fun onClosed(eventSource: EventSource) {
        println("SSE Connection Closed")
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        println("Error occurred: $t")
    }
}