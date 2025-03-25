package com.d104.data.remote.api

import com.d104.data.remote.dto.EnterRoomRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface MultiApiService {
    @POST("api/multi/lobby/enter")
    suspend fun enterRoom(
        @Body enterRoomRequest: EnterRoomRequestDto
    ): Boolean
}