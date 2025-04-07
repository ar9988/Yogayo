package com.d104.data.remote.api

import okhttp3.sse.EventSourceListener

interface SseApiService  {
    fun startSse(searchText: String, page: Int, listener: EventSourceListener)

    fun stopSse()
}