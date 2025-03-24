package com.d104.data.repository

import com.d104.data.mapper.RoomMapper
import com.d104.data.remote.api.SseApiService
import com.d104.data.remote.api.SseApiServiceImpl
import com.d104.data.remote.listener.EventListener
import com.d104.domain.model.Room
import com.d104.domain.repository.LobbyRepository
import com.launchdarkly.eventsource.background.BackgroundEventSource
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LobbyRepositoryImpl @Inject constructor(
    private val sseApiService: SseApiService,
    private val eventListener: EventListener,
    private val roomMapper: RoomMapper
) : LobbyRepository {
//    private val eventListener: EventListener = EventListener()
    private fun startSse(searchText: String, page: Int) {
        sseApiService.startSse(searchText, 1, eventListener)
    }

    override fun stopSse() {
        sseApiService.stopSse()
    }

    override suspend fun enterRoom(roomId: Int, password: String): Flow<Result<Boolean>> {
        TODO("Not yet implemented")
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