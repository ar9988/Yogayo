package com.d104.domain.repository

import com.d104.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface LobbyRepository {
    suspend fun getRooms(searchText:String, page:Int) : Flow<Result<List<Room>>>
    fun stopSse()
    suspend fun enterRoom(roomId: Int, password: String): Flow<Result<Boolean>>
}