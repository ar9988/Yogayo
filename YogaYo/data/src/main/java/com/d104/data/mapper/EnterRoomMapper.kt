package com.d104.data.mapper

import com.d104.domain.model.EnterResult
import javax.inject.Inject

class EnterRoomMapper @Inject constructor() : Mapper<Boolean, EnterResult> {
    override fun map(input: Boolean): EnterResult {
        return EnterResult.Success
    }
}