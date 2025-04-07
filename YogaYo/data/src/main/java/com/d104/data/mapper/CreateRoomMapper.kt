package com.d104.data.mapper

import com.d104.data.remote.dto.RoomResponseDto
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import javax.inject.Inject

class CreateRoomMapper @Inject constructor(
    private val yogaPoseMapper: YogaPoseMapper
) : Mapper<RoomResponseDto, CreateRoomResult> {

    override fun map(input: RoomResponseDto): CreateRoomResult {
        return CreateRoomResult.Success(
            Room(
                roomId = input.roomId,
                userId = input.userId,
                roomMax = input.roomMax,
                roomCount = input.roomCount,
                roomName = input.roomName,
                isPassword = input.hasPassword,
                userNickname = input.userNickname,
                userCourse = UserCourse(
                    courseId = -1,
                    courseName = "TODO()",
                    tutorial = false,
                    poses = yogaPoseMapper.mapToDomainList(input.pose)
                )
            )
        )
    }
}