package com.d104.domain.usecase

import com.d104.domain.model.LoginResult
import com.d104.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {

    suspend operator fun invoke(id: String, password: String): Flow<Result<LoginResult>> {
        return authRepository.login(id, password)
    }

}