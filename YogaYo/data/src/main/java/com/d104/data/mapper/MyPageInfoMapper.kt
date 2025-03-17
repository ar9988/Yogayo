package com.d104.data.mapper

import com.d104.data.remote.dto.MyPageInfoDto
import com.d104.domain.model.MyPageInfo
import javax.inject.Inject

class MyPageInfoMapper @Inject constructor() : Mapper<MyPageInfoDto, MyPageInfo> {
    override fun map(input: MyPageInfoDto): MyPageInfo {
        return MyPageInfo(
            userid = input.id,
            userNickName = input.nickName
        )
    }
}