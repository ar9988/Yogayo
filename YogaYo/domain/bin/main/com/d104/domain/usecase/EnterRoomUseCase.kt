package com.d104.domain.usecase

import com.d104.domain.model.EnterResult
import com.d104.domain.repository.LobbyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EnterRoomUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
){
    suspend operator fun invoke(roomId: Long, password: String) : Flow<Result<EnterResult>> {
        return lobbyRepository.enterRoom(roomId,password)
    }
}