package com.d104.domain.usecase

import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.Room
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.repository.LobbyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateRoomUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository
)
{
    suspend operator fun invoke(
        roomName: String,
        roomMax: Int,
        isPassword: Boolean,
        password: String,
        poses: List<YogaPose>
    ) :Flow<Result<CreateRoomResult>> {
        return lobbyRepository.createRoom(
            roomName,
            roomMax,
            isPassword,
            password,
            poses
        )
    }
}