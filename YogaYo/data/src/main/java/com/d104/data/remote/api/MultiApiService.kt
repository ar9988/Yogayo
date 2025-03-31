package com.d104.data.remote.api

import com.d104.data.remote.dto.CreateRoomRequestDto
import com.d104.data.remote.dto.EnterRoomRequestDto
import com.d104.data.remote.dto.RoomDto
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.UserCourse
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
        @Body createRoomRequestDto: CreateRoomRequestDto
    ): RoomDto
}