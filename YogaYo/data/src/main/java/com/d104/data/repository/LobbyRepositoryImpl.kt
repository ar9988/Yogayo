package com.d104.data.repository

import android.util.Log
import com.d104.data.mapper.CreateRoomMapper
import com.d104.data.mapper.EnterRoomMapper
import com.d104.data.mapper.PoseMapper
import com.d104.data.mapper.RoomMapper
import com.d104.data.mapper.YogaPoseMapper
import com.d104.data.remote.api.MultiApiService
import com.d104.data.remote.api.SseApiService
import com.d104.data.remote.dto.CourseRequestDto
import com.d104.data.remote.dto.CreateRoomRequestDto
import com.d104.data.remote.dto.EnterRoomRequestDto
import com.d104.data.remote.listener.EventListener
import com.d104.data.utils.ErrorUtils
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.EnterResult
import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.repository.LobbyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class LobbyRepositoryImpl @Inject constructor(
    private val sseApiService: SseApiService,
    private val eventListener: EventListener,
    private val multiApiService: MultiApiService,
    private val roomMapper: RoomMapper,
    private val createRoomMapper: CreateRoomMapper,
    private val enterRoomMapper: EnterRoomMapper,
    private val poseMapper: YogaPoseMapper
) : LobbyRepository {
    //    private val eventListener: EventListener = EventListener()
    private fun startSse(searchText: String, page: Int) {
        sseApiService.startSse(searchText, page, eventListener)
    }

    override fun stopSse() {
        sseApiService.stopSse()
    }

    override suspend fun enterRoom(roomId: Long, password: String): Flow<Result<EnterResult>> {
        return flow {
            try {
                val enterRoomRequestDto = EnterRoomRequestDto(roomId, password)
                val apiResponse = multiApiService.enterRoom(enterRoomRequestDto)

                val enterResult: EnterResult = enterRoomMapper.map(apiResponse)

                // Mapper 결과에 따라 분기
                if (enterResult is EnterResult.Success) {
                    // API 성공 & 비즈니스 로직 성공
                    emit(Result.success(enterResult)) // Result.success(EnterResult.Success)
                } else {
                    // API 성공 & 비즈니스 로직 실패 (비번 틀림 등)
                    // 이 경우도 API 호출 자체는 성공했으므로 Result.success로 감싸지만,
                    // 내부 값은 EnterResult.Error 타입임을 ViewModel에서 인지해야 함.
                    emit(Result.success(enterResult)) // Result.success(EnterResult.Error.BadRequest)
                }

            } catch (e: HttpException) {
                // --- API 호출 자체가 실패한 경우 ---
                val errorMessage = try { // 에러 메시지 파싱 시도
                    ErrorUtils.parseHttpError(e)?.message ?: "HTTP Error Code: ${e.code()}"
                } catch (parseError: Exception) {
                    "HTTP Error Code: ${e.code()} (Error body parsing failed)"
                }
                // Result.failure를 사용하여 API 호출 실패를 명확히 전달
                emit(Result.failure(RuntimeException("방 입장 중 오류 발생: $errorMessage", e))) // 원본 예외(e)를 포함

            } catch (e: Exception) { // HttpException 외의 다른 예외 (네트워크 연결 끊김 등)
                emit(Result.failure(RuntimeException("방 입장 중 예상치 못한 오류 발생: ${e.message}", e)))
            }
        }
    }

    override suspend fun createRoom(
        roomName: String,
        roomMax: Int,
        isPassword: Boolean,
        password: String,
        userCourse: UserCourse
    ): Flow<Result<CreateRoomResult>> {
        try {
            val createRequestDto = CreateRoomRequestDto(
                roomName,
                roomMax,
                isPassword,
                password,
                poseMapper.modelToDto(userCourse.poses)
            )
            Log.d("Lobby",createRequestDto.toString())
            val createRoomResponseDto = multiApiService.createRoom(
                createRequestDto
            )
            Log.d("Lobby",createRoomResponseDto.toString())
            val createRoomResult = createRoomMapper.map(createRoomResponseDto)
            return flow {
                emit(Result.success(createRoomResult))
            }
        } catch (e: HttpException) {
            val errorResult = when (e.code()) {
                400 -> {
                    val errorBody = ErrorUtils.parseHttpError(e)
                    CreateRoomResult.Error.BadRequest(errorBody?.message ?: "Bad Request")
                }

                401 -> {
                    CreateRoomResult.Error.Unauthorized("Unauthorized")
                }

                else -> {
                    CreateRoomResult.Error.Unauthorized("Unknown Error")
                }
            }
            return flow {
                emit(Result.success(errorResult))
            }
        }
    }

    override suspend fun sendRoomRecord(
        roomId: String,
        totalRanking: Int,
        totalScore: Int
    ): Flow<Result<Unit>> = flow {
        try {
            // 1. Retrofit suspend fun 직접 호출 (반환 타입이 Unit이라고 가정)
            multiApiService.sendRoomRecord(roomId, totalRanking, totalScore)

            // 2. 예외가 발생하지 않으면 성공이므로 Result.success(Unit) 방출
            emit(Result.success(Unit))

        } catch (e: HttpException) {
            // 3. HTTP 관련 예외 처리
            val errorMessage = try {
                ErrorUtils.parseHttpError(e)?.message ?: "HTTP 오류: ${e.code()}"
            } catch (parseError: Exception) {
                "HTTP 오류: ${e.code()} (에러 본문 파싱 실패)"
            }
            emit(Result.failure(RuntimeException(errorMessage, e)))

        } catch (e: IOException) {
            // 4. 네트워크 연결 관련 예외 처리
            emit(Result.failure(RuntimeException("네트워크 오류: ${e.message}", e)))

        } catch (e: Exception) {
            // 5. 기타 예상치 못한 예외 처리
            emit(Result.failure(RuntimeException("알 수 없는 오류: ${e.message}", e)))
        }
    }

    override suspend fun getRooms(searchText: String, page: Int): Flow<Result<List<Room>>> {
        startSse(searchText, page)

        return eventListener.sseEvents.map { event ->
            try {
                // 이벤트 데이터를 Room 객체로 변환하는 로직 작성
                val rooms = roomMapper.map(event)
                println("Event Data: $rooms")
                Result.success(rooms)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }.distinctUntilChanged()
    }
}