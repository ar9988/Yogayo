package com.d104.data.mapper

import com.d104.data.remote.dto.SignUpResponseDto
import com.d104.domain.model.LoginResult
import com.d104.domain.model.SignUpResult
import javax.inject.Inject

class SignUpMapper @Inject constructor():Mapper<Boolean,SignUpResult> {
    override fun map(input: Boolean): SignUpResult {
        return SignUpResult.Success
    }
}