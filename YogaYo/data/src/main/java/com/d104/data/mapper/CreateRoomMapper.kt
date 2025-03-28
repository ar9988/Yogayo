package com.d104.data.mapper

import com.d104.data.remote.dto.CreateRoomResponseDto
import com.d104.domain.model.CreateRoomResult
import javax.inject.Inject

class CreateRoomMapper @Inject constructor() : Mapper<CreateRoomResponseDto, CreateRoomResult> {

    override fun map(input: CreateRoomResponseDto): CreateRoomResult {
        return if(input.success) {
            CreateRoomResult.Success(input.id)
        } else {
            if(input.id == 401L) {
                return CreateRoomResult.Error.Unauthorized("Unauthorized")
            }
            else CreateRoomResult.Error.BadRequest("Failed to create room")
        }
    }
}