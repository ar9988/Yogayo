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
        // 먼저 selectedCourse가 null인지 확인합니다.
        if (uiState.value.roomTitle == "") {
            processIntent(MultiIntent.CreateRoomFail("방 제목을 입력해주세요")) // 사용자에게 보여줄 메시지 수정 가능
            Timber.w("createRoom cancelled: title is null.") // 로그 기록
            return // 더 이상 진행하지 않고 함수 종료
        }

        if (uiState.value.selectedCourse == null) {
            // selectedCourse가 null이면 즉시 실패 처리하고 함수를 종료합니다.
            processIntent(MultiIntent.CreateRoomFail("오류 발생: 코스를 선택해주세요.")) // 사용자에게 보여줄 메시지 수정 가능
            Timber.w("createRoom cancelled: selectedCourse is null.") // 로그 기록
            return // 더 이상 진행하지 않고 함수 종료
        }

        // selectedCourse가 null이 아닐 경우에만 방 생성 로직을 진행합니다.
        viewModelScope.launch {
            Timber.d("roomstate:${uiState.value.roomMax} ${uiState.value.roomPassword} ${uiState.value.isPassword}")
            createRoomUseCase(
                uiState.value.roomTitle,
                uiState.value.roomMax,
                uiState.value.isPassword,
                uiState.value.roomPassword,
                // 이제 selectedCourse는 null이 아님이 보장됩니다. (하지만 안전하게 !! 제거 가능)
                uiState.value.selectedCourse!! // 또는 그냥 uiState.value.selectedCourse 사용 가능
            ).collect { result ->
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
                            // 다른 성공/실패 케이스 처리 ...
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