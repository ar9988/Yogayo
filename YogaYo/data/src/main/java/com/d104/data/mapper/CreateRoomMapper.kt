package com.d104.data.mapper

import com.d104.data.remote.dto.RoomDto
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.Room
import javax.inject.Inject

class CreateRoomMapper @Inject constructor() : Mapper<RoomDto, CreateRoomResult> {

    override fun map(input: RoomDto): CreateRoomResult {
        return CreateRoomResult.Success(
            Room(
                roomId = input.roomId,
                userNickname = input.userNickname,
                roomMax = input.roomMax,
                roomCount = input.roomCount,
                roomName = input.roomName,
                isPassword = input.isPassword,
                userCourse = input.userCourse
            )
        )
    }
}