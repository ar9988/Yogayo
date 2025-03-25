package com.d104.data.mapper

import com.d104.domain.model.Room
import javax.inject.Inject

class RoomMapper @Inject constructor(): Mapper<String, List<Room>>{
    override fun map(input: String): List<Room> {
        return emptyList<Room>()
    }
}