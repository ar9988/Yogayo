package com.d104.domain.usecase

import com.d104.domain.repository.LobbyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendRoomRecordUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository
) {
    suspend operator fun invoke(roomId: String, totalRanking: Int,totalScore:Int): Flow<Result<Unit>> {
        return lobbyRepository.sendRoomRecord(roomId, totalRanking,totalScore)
    }
}