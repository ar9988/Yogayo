package com.d104.data.mapper

import com.d104.data.remote.dto.SignUpResponseDto
import com.d104.domain.model.SignUpResult
import javax.inject.Inject

class SignUpMapper @Inject constructor():Mapper<SignUpResponseDto,SignUpResult> {
    override fun map(input: SignUpResponseDto): SignUpResult {
        return when {
            input.success -> SignUpResult.Success
            input.status == 400 -> SignUpResult.Error.BadRequest(input.message ?: "Bad Request")
            input.status == 409 -> SignUpResult.Error.ConflictUser(input.message ?: "User already exists")
            else -> SignUpResult.Error.BadRequest("Unknown error occurred")
        }
    }
}