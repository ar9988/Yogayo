package com.d104.domain.usecase

import com.d104.domain.model.Room
import com.d104.domain.repository.LobbyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoomUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
) {
    suspend operator fun invoke(searchText:String, page:Int) : Flow<Result<List<Room>>> {
        return lobbyRepository.getRooms(searchText, page)
    }
}