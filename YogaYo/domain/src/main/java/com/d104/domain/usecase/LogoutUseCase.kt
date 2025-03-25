package com.d104.domain.usecase

import com.d104.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
){
    suspend operator fun invoke(): Flow<Boolean> {
        return dataStoreRepository.clearUserData()
    }
}