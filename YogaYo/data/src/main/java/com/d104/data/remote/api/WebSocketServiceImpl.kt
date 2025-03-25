package com.d104.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketServiceImpl @Inject constructor(
    private val client: OkHttpClient,
) : WebSocketService{

    override fun connect(url:String,listener: WebSocketListener) {
        // connect to websocket
    }

    override fun disconnect() {
        // disconnect from websocket
    }

    override fun send(message: String) {
        // send message to websocket
    }
}