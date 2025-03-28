package com.d104.domain.repository

import kotlinx.coroutines.flow.Flow

interface WebSocketRepository {
    suspend fun connect() : Flow<String>
    fun disconnect()
    fun send(message: String)
}