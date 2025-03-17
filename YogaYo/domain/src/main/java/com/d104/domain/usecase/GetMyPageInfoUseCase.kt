package com.d104.domain.usecase

import com.d104.domain.model.MyPageInfo
import com.d104.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyPageInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<Result<MyPageInfo>> =
        userRepository.getMyPageInfo()
}