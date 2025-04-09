package com.d104.domain.usecase

import com.d104.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserNickNameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String {
        return authRepository.getUserName()
    }
}
