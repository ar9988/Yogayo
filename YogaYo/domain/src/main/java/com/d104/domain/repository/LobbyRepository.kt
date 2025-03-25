package com.d104.domain.repository

import com.d104.domain.model.EnterResult
import com.d104.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface LobbyRepository {
    suspend fun getRooms(searchText:String, page:Int) : Flow<Result<List<Room>>>
    fun stopSse()
    suspend fun enterRoom(roomId: Long, password: String): Flow<Result<EnterResult>>
}