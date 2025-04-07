package com.d104.yogaapp.features.multi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.usecase.CancelSearchStreamUseCase
import com.d104.domain.usecase.CreateRoomUseCase
import com.d104.domain.usecase.EnterRoomUseCase
import com.d104.domain.usecase.GetUserCourseUseCase
import com.d104.domain.usecase.GetRoomUseCase
import com.d104.domain.usecase.UpdateCourseUseCase
import com.d104.yogaapp.utils.CourseJsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MultiViewModel @Inject constructor(
    private val multiReducer: MultiReducer,
    private val getRoomUseCase : GetRoomUseCase,
    private val cancelSearchStreamUseCase: CancelSearchStreamUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val getCourseUseCase: GetUserCourseUseCase,
    courseJsonParser: CourseJsonParser,
    private val enterRoomUseCase: EnterRoomUseCase,
    private val createRoomUseCase: CreateRoomUseCase
) : ViewModel(){
    private val _uiState = MutableStateFlow(MultiState())
    val uiState :StateFlow<MultiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private val tmpPoseInfo = listOf(
        YogaPose(1, "나무 자세", "https://d5sbbf6usl3xq.cloudfront.net/baddhakonasana.png", 1, listOf("나무 자세 설명"), "video_url", -1,""),
        YogaPose(2, "나무 자세", "https://d5sbbf6usl3xq.cloudfront.net/utthita_trikonasana_flip.png", 3, listOf("나무 자세 설명"), "video_url", 3,""),
        YogaPose(3, "전사 자세", "https://d5sbbf6usl3xq.cloudfront.net/utthita_parsvakonasana.png", 2, listOf("전사 자세 설명"), "video_url", 2,""),
        YogaPose(4, "다운독 자세", "https://d5sbbf6usl3xq.cloudfront.net/samasthiti.png", 2, listOf("다운독 자세 설명"), "video_url", -1,"")
    )
    fun processIntent(intent: MultiIntent){
        val newState = multiReducer.reduce(_uiState.value,intent)
        _uiState.value = newState
        when(intent){
            is MultiIntent.SearchRoom ->{
                _uiState.value.page = emptyList()
                loadRooms(newState.roomSearchText,newState.pageIndex)
            }
            is MultiIntent.EditCourse -> {
                updateCourse(intent.courseId,intent.courseName,intent.poses)
            }
            is MultiIntent.SearchCourse -> {
                searchCourse()
            }
            is MultiIntent.EnterRoom -> {
                enterRoom()
            }
            is MultiIntent.PrevPage -> {
                if(newState.pageIndex>0){
                    _uiState.value.page = emptyList()
                    loadRooms(newState.roomSearchText,newState.pageIndex)
                }
            }
            is MultiIntent.NextPage -> {
                _uiState.value.page = emptyList()
                loadRooms(newState.roomSearchText,newState.pageIndex)
            }
            is MultiIntent.SelectRoom -> {
                processIntent(MultiIntent.UpdateRoomPassword(""))
            }
            is MultiIntent.ClickCreateRoomButton -> {
                processIntent(MultiIntent.UpdateRoomTitle(""))
                processIntent(MultiIntent.UpdateRoomPassword(""))
            }
            is MultiIntent.CreateRoom ->{
                createRoom()
            }
            else -> {}
        }
    }

    private fun createRoom() {
        viewModelScope.launch {
            createRoomUseCase(
                uiState.value.roomTitle,
                uiState.value.roomMax,
                uiState.value.isPassword,
                uiState.value.roomPassword,
                uiState.value.selectedCourse!!
            ).collect{ result->
                result.fold(
                    onSuccess = { createRoomResult ->
                        when (createRoomResult) {
                            is CreateRoomResult.Success -> {
                                // 방 생성 성공 처리
                                processIntent(MultiIntent.SelectRoom(createRoomResult.room))
                                processIntent(MultiIntent.EnterRoom)
                            }
                            is CreateRoomResult.Error.BadRequest -> {
                                // 잘못된 요청 처리
                                processIntent(MultiIntent.CreateRoomFail("잘못된 요청: ${createRoomResult.message}"))
                            }
                            is CreateRoomResult.Error.Unauthorized -> {
                                // 인증 실패 처리
                                processIntent(MultiIntent.CreateRoomFail("인증 실패: ${createRoomResult.message}"))
                            }
                        }
                    },
                    onFailure = { throwable ->
                        // 네트워크 오류 또는 예외 처리
                        processIntent(MultiIntent.CreateRoomFail("오류 발생: ${throwable.message}"))
                    }
                )
            }
        }
    }

    private fun enterRoom(){
        viewModelScope.launch {
            enterRoomUseCase(uiState.value.selectedRoom!!.roomId, uiState.value.roomPassword).collect{ result->
                result.onSuccess {
                    processIntent(MultiIntent.EnterRoomComplete)
                }
                result.onFailure {
                    processIntent(MultiIntent.EnterRoomFail(it.message ?: "방 입장에 실패했습니다."))
                }
            }

        }
    }

    // 방 목록 조회 함수
    private fun loadRooms(searchText: String, pageIndex: Int) {
        cancelSearch()
        searchJob = viewModelScope.launch {
            getRoomUseCase(searchText,pageIndex).collect { result ->
                Timber.tag("SSE").d(result.toString())
                result.onSuccess {
                    Timber.tag("SSE").d(it.toString())
                    processIntent(MultiIntent.UpdatePage(it))
                    processIntent(MultiIntent.RoomLoaded)
                }
                result.onFailure {
                    // 에러 처리
                }
            }
        }
    }

    private fun searchCourse(){
//        getCourseUseCase()
//        getCourseUseCase.collect{ result ->
//            result.onSuccess {
//                _uiState.value.yogaCourses = it
//                processIntent(MultiIntent.RoomLoaded)
//            }
//            result.onFailure {
//                // 에러 처리
//            }
//        }
    }

    private fun updateCourse(courseId:Long, courseName:String, poses:List<YogaPoseWithOrder>){
        updateCourseUseCase
    }

    private fun cancelSearch() {
        searchJob?.cancel()
        cancelSearchStreamUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        cancelSearch()
    }

    init {
        searchCourse()
        loadRooms("", uiState.value.pageIndex)

        _uiState.value.yogaCourses = courseJsonParser.loadUserCoursesFromAssets("courseSet.json")
    }
}