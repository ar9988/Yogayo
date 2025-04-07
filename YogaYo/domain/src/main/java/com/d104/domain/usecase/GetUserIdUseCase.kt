package com.d104.domain.usecase

import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.UserRepository
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String {
        return authRepository.getUserId()
    }
}