package com.d104.domain.usecase

import com.d104.domain.model.UserInfo
import com.d104.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<UserInfo> = userRepository.getUserInfo()
}