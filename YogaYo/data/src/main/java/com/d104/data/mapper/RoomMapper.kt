package com.d104.data.mapper

import android.util.Log
import com.d104.data.remote.dto.YogaPoseDto
import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject
class RoomMapper @Inject constructor(
    private val yogaPoseMapper: YogaPoseMapper, // Assumes this maps YogaPoseDto -> YogaPose
    private val json: Json // kotlinx.serialization Json instance
) : Mapper<String, List<Room>> {

    override fun map(input: String): List<Room> {
        val roomList = mutableListOf<Room>()

        // Handle empty or invalid input early
        if (input.isBlank()) {
            Log.d("W","Input JSON string is blank.")
            return roomList
        }

        try {
            val jsonArray = JSONArray(input)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                // --- Extract Basic Room Details ---
                val roomId = jsonObject.getLong("roomId")
                val userNickname = jsonObject.getString("userNickname")
                val roomMax = jsonObject.getInt("roomMax")
                val roomCount = jsonObject.getInt("roomCount")
                val roomName = jsonObject.getString("roomName")
                val hasPassword = jsonObject.getBoolean("hasPassword") // Field from JSON

                // --- Parse Pose Array using kotlinx.serialization ---
                val poseJsonArray = jsonObject.getJSONArray("pose")
                val poseJsonString = poseJsonArray.toString() // Convert JSONArray to String for kotlinx

                // Decode the JSON string into a list of DTOs
                val poseDtoList: List<YogaPoseDto> = json.decodeFromString(poseJsonString)

                // Map DTOs to Domain Models using YogaPoseMapper
                val poses: List<YogaPose> = poseDtoList.map { dto ->
                    Log.d("Mapper",dto.toString())
                    yogaPoseMapper.mapToDomain(dto) // Use the injected mapper
                }

                // --- Create UserCourse (with potentially hardcoded values) ---
                // If courseId/Name/Tutorial were part of the room JSON, extract them here.
                // Otherwise, these defaults are used.
                val userCourse = UserCourse(
                    poses = poses,
                    courseId = -1,             // Default/Placeholder
                    courseName = "Multiplayer Course", // Or derive from roomName? Or keep generic?
                    tutorial = false           // Default/Placeholder
                )

                // --- Create Room Domain Model ---
                val room = Room(
                    roomId = roomId,
                    userNickname = userNickname,
                    roomMax = roomMax,
                    roomCount = roomCount,
                    roomName = roomName,
                    isPassword = hasPassword,  // Map hasPassword to isPassword
                    userCourse = userCourse
                )

                roomList.add(room)
            }
        } catch (e: JSONException) {
            // Handle JSON parsing errors specifically
            Log.e("error", "Failed to parse room list JSON")
            // Depending on requirements, you might return empty list, throw exception, etc.
        } catch (e: Exception) {
            // Handle other potential errors (e.g., kotlinx.serialization errors, mapping errors)
            Log.e("error", "An unexpected error occurred during room mapping")
            e.printStackTrace() // Keep for debugging if needed, but prefer logging
        }

        return roomList
    }
}