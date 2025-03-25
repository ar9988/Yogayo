package com.d104.domain.usecase

import com.d104.domain.repository.LobbyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EnterRoomUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
){
    suspend operator fun invoke(roomId: Int, password: String) : Flow<Result<Boolean>> {
        return lobbyRepository.enterRoom(roomId,password)
    }
}