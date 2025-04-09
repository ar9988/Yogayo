package com.d104.domain.repository

import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.EnterResult
import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseWithOrder
import kotlinx.coroutines.flow.Flow

interface LobbyRepository {
    suspend fun getRooms(searchText:String, page:Int) : Flow<Result<List<Room>>>
    fun stopSse()
    suspend fun enterRoom(roomId: Long, password: String): Flow<Result<EnterResult>>
    suspend fun createRoom(
        roomName: String,
        roomMax: Int,
        isPassword: Boolean,
        password: String,
        userCourse: UserCourse
    ): Flow<Result<CreateRoomResult>>

    suspend fun sendRoomRecord(roomId: String, totalRanking: Int, totalScore: Int): Flow<Result<Unit>>
}