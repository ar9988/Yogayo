package com.d104.data.mapper

import com.d104.data.remote.dto.MyPageInfoResponseDto
import com.d104.domain.model.MyPageInfo
import javax.inject.Inject

class MyPageInfoMapper @Inject constructor() : Mapper<MyPageInfoResponseDto, MyPageInfo> {
    override fun map(input: MyPageInfoResponseDto): MyPageInfo {
        return MyPageInfo(
            userId = input.userId,
            userName = input.userName,
            userNickName = input.userNickName,
            userProfile = input.userProfile,
            exDays = input.exDays?:0,
            exConDays = input.exConDays?:0,
            roomWin = input.roomWin?:0,
        )
    }
}