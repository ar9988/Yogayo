package com.d104.data.mapper

import com.d104.data.remote.dto.LoginResponseDto
import com.d104.domain.model.LoginResult
import javax.inject.Inject

class LoginMapper @Inject constructor() :Mapper<LoginResponseDto,LoginResult> {
    override fun map(input: LoginResponseDto): LoginResult {
        return LoginResult.Success(
            userId = input.userId,
            userLoginId = input.userLoginId,
            userName = input.userName,
            userNickname = input.userNickname,
            userProfile = input.userProfile,
            accessToken = input.accessToken,
            refreshToken = input.refreshToken
        )
    }

}