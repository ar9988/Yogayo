package com.d104.data.mapper

import com.d104.data.remote.dto.UserInfoDto
import com.d104.domain.model.UserInfo
import javax.inject.Inject

class UserInfoMapper @Inject constructor() : Mapper<UserInfoDto, UserInfo> {
    override fun map(input: UserInfoDto): UserInfo {
        return UserInfo(
            userid = input.id,
            userNickName = input.nickName
        )
    }
}