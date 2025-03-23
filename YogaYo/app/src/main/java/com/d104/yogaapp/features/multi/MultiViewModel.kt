package com.d104.yogaapp.features.multi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.usecase.CancelSearchStreamUseCase
import com.d104.domain.usecase.GetRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MultiViewModel @Inject constructor(
    private val multiReducer: MultiReducer,
    private val getRoomUseCase : GetRoomUseCase,
    private val cancelSearchStreamUseCase: CancelSearchStreamUseCase
) : ViewModel(){
    private val _uiState = MutableStateFlow(MultiState())
    val uiState :StateFlow<MultiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()
    val tmpPoseInfo = listOf(
        YogaPose(1, "나무 자세", "https://d5sbbf6usl3xq.cloudfront.net/baddhakonasana.png", 1, "나무 자세 설명", "video_url", -1),
        YogaPose(2, "나무 자세", "https://d5sbbf6usl3xq.cloudfront.net/utthita_trikonasana_flip.png", 3, "나무 자세 설명", "video_url", 3),
        YogaPose(3, "전사 자세", "https://d5sbbf6usl3xq.cloudfront.net/utthita_parsvakonasana.png", 2, "전사 자세 설명", "video_url", 2),
        YogaPose(4, "다운독 자세", "https://d5sbbf6usl3xq.cloudfront.net/samasthiti.png", 2, "다운독 자세 설명", "video_url", -1)
    )
    fun processIntent(intent: MultiIntent){
        val newState = multiReducer.reduce(_uiState.value,intent)
        _uiState.value = newState
        when(intent){
            is MultiIntent.SearchRoom ->{
                _uiState.value.page = rooms.value
                loadRooms(newState.searchText,newState.pageIndex)
            }
            else -> {}
        }
    }

    // 방 목록 조회 함수
    fun loadRooms(searchText: String, pageIndex: Int) {
        cancelSearch()
        searchJob = viewModelScope.launch {
            getRoomUseCase(searchText,pageIndex).collect { result ->
                result.onSuccess {
                    _rooms.value = it
                    processIntent(MultiIntent.RoomLoaded)
                }
                result.onFailure {
                    // 에러 처리
                }
            }
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        cancelSearchStreamUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        cancelSearch()
    }

    // 상태 초기화 함수
    fun clearRooms() {
        _rooms.value = emptyList()
    }

    init {
        _rooms.value = listOf(
            Room(
                roomId = 0,
                userNickName = "We'T",
                roomMax = 6,
                roomCount = 4,
                roomName = "요가 할래?",
                isPassword = true,
                course = UserCourse(
                    courseId = 1,
                    courseName = "Test Course",
                    tutorial = false,
                    poses = tmpPoseInfo
                )
            ),
            Room(
                roomId = 0,
                userNickName = "TestNickName",
                roomMax = 6,
                roomCount = 1,
                roomName = "Test Room Name",
                isPassword = false,
                course = UserCourse(
                    courseId = 1,
                    courseName = "Test Course",
                    tutorial = false,
                    poses = tmpPoseInfo
                )
            ),
            Room(
                roomId = 0,
                userNickName = "TestNickName",
                roomMax = 6,
                roomCount = 1,
                roomName = "Test Room Name",
                isPassword = false,
                course = UserCourse(
                    courseId = 1,
                    courseName = "Test Course",
                    tutorial = false,
                    poses = tmpPoseInfo
                )
            ),
            Room(
                roomId = 0,
                userNickName = "TestNickName",
                roomMax = 6,
                roomCount = 1,
                roomName = "Test Room Name",
                isPassword = false,
                course = UserCourse(
                    courseId = 1,
                    courseName = "Test Course",
                    tutorial = false,
                    poses = tmpPoseInfo
                )
            ),
            Room(
                roomId = 0,
                userNickName = "TestNickName",
                roomMax = 6,
                roomCount = 1,
                roomName = "Test Room Name",
                isPassword = false,
                course = UserCourse(
                    courseId = 1,
                    courseName = "Test Course",
                    tutorial = false,
                    poses = tmpPoseInfo
                )
            )
        )

    }
}