package com.d104.data.remote.api


import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.HttpUrl
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources.createFactory
import javax.inject.Inject

class SseApiServiceImpl @Inject constructor(
    private val client: OkHttpClient
) : SseApiService {
    private var eventSource: EventSource? = null
    override fun startSse(searchText: String, page: Int, listener: EventSourceListener) {

        val url = HttpUrl.Builder()
            .scheme("http")
            .host("j12d104.p.ssafy.io")
            .port(8080)
            .addPathSegment("api")
            .addPathSegment("multi")
            .addPathSegment("lobby")
            .addQueryParameter("roomName", searchText)
            .addQueryParameter("page", page.toString())
            .build()

        val request = Request.Builder()
            .url(url)
            .build()
        val factory = createFactory(client)
        eventSource = factory.newEventSource(request,listener)
    }

    override fun stopSse() {
        eventSource?.cancel()
        eventSource = null
    }


}