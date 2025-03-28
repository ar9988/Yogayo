package com.d104.data.remote.api

import okhttp3.WebSocketListener

interface WebSocketService {
    fun connect(url:String,listener: WebSocketListener)
    fun disconnect()
    fun send(message: String)
}