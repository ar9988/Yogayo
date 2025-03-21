package com.d104.domain.usecase

import com.d104.domain.model.Room
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoomUseCase @Inject constructor(

) {
    suspend operator fun invoke() : Flow<Result<List<Room>>> {
        TODO()
    }
}