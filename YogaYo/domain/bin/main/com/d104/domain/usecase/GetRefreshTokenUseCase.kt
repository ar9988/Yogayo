package com.d104.domain.usecase

import com.d104.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRefreshTokenUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    operator fun invoke(): Flow<String?> = dataStoreRepository.getRefreshToken()

}