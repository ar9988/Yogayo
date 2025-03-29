package com.d104.data.repository

import com.d104.data.mapper.CreateRoomMapper
import com.d104.data.mapper.EnterRoomMapper
import com.d104.data.mapper.PoseMapper
import com.d104.data.mapper.RoomMapper
import com.d104.data.remote.api.MultiApiService
import com.d104.data.remote.api.SseApiService
import com.d104.data.remote.dto.EnterRoomRequestDto
import com.d104.data.remote.utils.EventListener
import com.d104.data.utils.ErrorUtils
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.EnterResult
import com.d104.domain.model.Room
import com.d104.domain.model.YogaPose
import com.d104.domain.repository.LobbyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject

class LobbyRepositoryImpl @Inject constructor(
    private val sseApiService: SseApiService,
    private val eventListener: EventListener,
    private val multiApiService: MultiApiService,
    private val roomMapper: RoomMapper,
    private val createRoomMapper: CreateRoomMapper,
    private val enterRoomMapper: EnterRoomMapper,
    private val poseMapper: PoseMapper
) : LobbyRepository {
//    private val eventListener: EventListener = EventListener()
    private fun startSse(searchText: String, page: Int) {
        sseApiService.startSse(searchText, 1, eventListener)
    }

    override fun stopSse() {
        sseApiService.stopSse()
    }

    override suspend fun enterRoom(roomId: Long, password: String): Flow<Result<EnterResult>> {
        return flow {
            try {
                val enterRoomRequestDto = EnterRoomRequestDto(
                    roomId,
                    password
                )
                val signUpResult = enterRoomMapper.map(multiApiService.enterRoom(enterRoomRequestDto))
                emit(Result.success(signUpResult))
            } catch (e: HttpException) {
                val errorResult = when (e.code()){
                    400 -> {
                        val errorBody = ErrorUtils.parseHttpError(e)
                        EnterResult.Error.BadRequest(errorBody?.message ?: "Bad Request")
                    }
                    else ->{
                        EnterResult.Error.Unauthorized("Unknown Error")
                    }
                }
                emit(Result.success(errorResult))
            }
        }
    }

    override suspend fun createRoom(
        roomName: String,
        roomMax: Int,
        isPassword: Boolean,
        password: String,
        poses: List<YogaPose>
    ): Flow<Result<CreateRoomResult>> {
        try {
            val createRoomResponseDto = multiApiService.createRoom(
                roomName,
                roomMax,
                isPassword,
                password,
                poseMapper.map(poses)
            )
            val createRoomResult = createRoomMapper.map(createRoomResponseDto)
            return flow {
                emit(Result.success(createRoomResult))
            }
        } catch (e: HttpException) {
            val errorResult = when (e.code()){
                400 -> {
                    val errorBody = ErrorUtils.parseHttpError(e)
                    CreateRoomResult.Error.BadRequest(errorBody?.message ?: "Bad Request")
                }
                401 -> {
                    CreateRoomResult.Error.Unauthorized("Unauthorized")
                }
                else ->{
                    CreateRoomResult.Error.Unauthorized("Unknown Error")
                }
            }
            return flow {
                emit(Result.success(errorResult))
            }
        }
    }

    override suspend fun getRooms(searchText: String, page: Int): Flow<Result<List<Room>>> {
        startSse(searchText,page)

        return eventListener.sseEvents.map { event ->
            try {
                // 이벤트 데이터를 Room 객체로 변환하는 로직 작성
                val rooms = roomMapper.map(event)
                Result.success(rooms)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }.distinctUntilChanged()
    }
}