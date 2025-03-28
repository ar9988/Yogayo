package com.d104.data.remote.api

import com.d104.data.remote.dto.CreateRoomResponseDto
import com.d104.data.remote.dto.EnterRoomRequestDto
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.YogaPoseWithOrder
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.POST

interface MultiApiService {
    @POST("api/multi/lobby/enter")
    suspend fun enterRoom(
        @Body enterRoomRequest: EnterRoomRequestDto
    ): Boolean

    @POST("api/multi/lobby")
    suspend fun createRoom(
        roomName: String,
        roomMax: Int,
        isPassword: Boolean,
        password: String,
        poses: List<YogaPoseWithOrder>
    ): CreateRoomResponseDto
}