package com.d104.yogaapp.features.multi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.CreateRoomResult
import com.d104.domain.model.EnterResult
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
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MultiViewModel @Inject constructor(
    private val multiReducer: MultiReducer,
    private val getRoomUseCase : GetRoomUseCase,
    private val cancelSearchStreamUseCase: CancelSearchStreamUseCase,
    private val getCourseUseCase: GetUserCourseUseCase,
    courseJsonParser: CourseJsonParser,
    private val enterRoomUseCase: EnterRoomUseCase,
    private val createRoomUseCase: CreateRoomUseCase
) : ViewModel(){
    private val _uiState = MutableStateFlow(MultiState())
    val uiState :StateFlow<MultiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    fun processIntent(intent: MultiIntent){
        val newState = multiReducer.reduce(_uiState.value,intent)
        _uiState.value = newState
        when(intent){
            is MultiIntent.SearchRoom ->{
                _uiState.value.page = emptyList()
                loadRooms(newState.roomSearchText,newState.pageIndex)
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
                uiState.value.selectedCourse!!
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
        Timber.tag("EnterRoom").d("Entering room with ID: ${uiState.value.selectedRoom?.roomId} and password: ${uiState.value.roomPassword}")
        viewModelScope.launch {
            enterRoomUseCase(uiState.value.selectedRoom!!.roomId, uiState.value.roomPassword).collect { result ->
                result.onSuccess { enterResult -> // API 호출 성공 시 진입
                    Timber.tag("EnterRoom").d("API Call Success. EnterResult: $enterResult") // 여기 로그는 BadRequest로 찍힘
                    // --- 여기가 중요 ---
                    if (enterResult is EnterResult.Success) {
                        // enterResult가 EnterResult.Success 타입일 때만 이 블록 안으로 들어와야 함
                        Timber.tag("EnterRoom")
                            .d(">>> EnterResult is Success type. Processing Complete.") // 확인용 로그 추가
                        processIntent(MultiIntent.EnterRoomSuccess)

                    } else if (enterResult is EnterResult.Error) {
                        // enterResult가 EnterResult.Error 타입일 때만 이 블록 안으로 들어와야 함
                        Timber.tag("EnterRoom").d(">>> EnterResult is Error type. Processing Fail.") // 확인용 로그 추가
//                        val errorMessage = when (enterResult) {
//                            is EnterResult.Error.BadRequest -> enterResult.message
//                            // 다른 EnterResult.Error 타입들... (필요 시 추가)
//                            // is EnterResult.Error.Unauthorized -> enterResult.message // 예시
//                            else -> "알 수 없는 입장 오류" // 모든 Error 타입을 처리하거나 기본 메시지
//                        }
                        val errorMessage = "입장 할 수 없습니다."
                        processIntent(MultiIntent.EnterRoomFail(errorMessage ?: "방 입장에 실패했습니다."))

                    }

                }
                result.onFailure { exception -> // API 호출 자체가 실패했을 때 진입
                    Timber.tag("EnterRoom").e(exception, "API Call Failed: ${exception.message}")
                    processIntent(MultiIntent.EnterRoomFail(exception.message ?: "알 수 없는 오류로 방 입장에 실패했습니다."))
                }
            }
        }
    }

    // 방 목록 조회 함수
    private fun loadRooms(searchText: String, pageIndex: Int, isManualSearch: Boolean = false) {
        // 현재 진행 중인 작업이 있고, 수동 검색 요청이 아니라면 중복 실행 방지 (선택적)
        if (!isManualSearch && searchJob?.isActive == true) {
            Timber.d("loadRooms skipped: search job is already active.")
            return
        }

        cancelSearch() // 새 로딩 시작 전 기존 작업 취소
        Timber.i("Starting loadRooms (SSE). Search: '$searchText', Page: $pageIndex, Manual: $isManualSearch")

        searchJob = viewModelScope.launch {
            try {
                getRoomUseCase(searchText, pageIndex)
                    .onCompletion { cause ->
                        // Flow가 정상적으로 완료되거나 오류로 종료될 때 호출됨
                        if (currentCoroutineContext().isActive) { // 코루틴이 외부에서 취소되지 않았는지 확인
                            Timber.i("SSE Flow completion detected. Cause: ${cause?.message ?: "Normal"}. Attempting restart after delay.")
                            // ViewModel이 활성 상태일 때만 재시작 시도
                            if (viewModelScope.isActive) {
                                launch { // 재시작 로직을 별도 코루틴으로 분리 (딜레이 영향 최소화)
                                    delay(1000)
                                    Timber.i("Restarting loadRooms due to SSE completion/error...")
                                    // 재시작 시 최신 상태 사용
                                    loadRooms(uiState.value.roomSearchText, uiState.value.pageIndex)
                                }
                            } else {
                                Timber.w("ViewModel scope inactive. Skipping SSE restart.")
                            }
                        } else {
                            Timber.w("SSE Flow completed, but coroutine was already cancelled. Skipping restart.")
                        }
                        // 완료 시 searchJob 참조를 null로 설정할지 여부 (선택적)
                        // searchJob = null // 재시작 로직에서 새 Job으로 덮어쓰므로 필수는 아님
                    }
                    .catch { throwable ->
                        // Flow 처리 중 발생하는 예외 처리 (onCompletion의 cause와 동일할 수 있음)
                        Timber.e(throwable, "Error caught during SSE flow collection.")
                        // 여기서 UI 에러 상태 업데이트 가능 (예: 스낵바 표시)
                        // processIntent(MultiIntent.ShowError("SSE 연결 오류: ${throwable.message}"))
                        // catch 블록 이후 onCompletion이 호출되므로, 재시작 로직은 onCompletion에 유지
                    }
                    .collect { result ->
                        // Flow에서 각 아이템(Result<List<RoomInfo>>)을 받았을 때 처리
                        if (!isActive) return@collect // 수집 중 코루틴 취소 시 처리 중단

                        Timber.tag("SSE").d("Received data via SSE: $result")
                        result.onSuccess { roomList ->
                            processIntent(MultiIntent.UpdatePage(roomList)) // 페이지 업데이트
                            processIntent(MultiIntent.RoomLoaded) // 로딩 완료 상태 (필요 시)
                        }
                        result.onFailure { apiError -> // Result 내부의 실패 처리
                            Timber.tag("SSE").e("API Error within SSE data: $apiError")
                            // 특정 API 오류에 대한 처리 (예: 잘못된 요청)
                            // processIntent(MultiIntent.ShowError("데이터 로딩 실패: ${apiError.message}"))
                        }
                    }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Timber.i("SSE loading coroutine cancelled.")
                    // 정상적인 취소이므로 특별한 처리 불필요
                } else {
                    Timber.e(e, "Unexpected exception during SSE setup/collection.")
                    // 예상치 못한 오류 처리 (예: 네트워크 설정 문제)
                    // processIntent(MultiIntent.ShowError("치명적 오류 발생"))
                    // 이 경우에도 onCompletion이 호출되지 않을 수 있으므로, 필요시 여기서 재시작 로직 추가 고려
                }
            } finally {
                Timber.d("SSE loading coroutine finished execution block.")
                // finally 블록은 항상 실행됨 (정상 완료, 예외, 취소 모두)
                // 여기서 searchJob = null 처리 가능성 있으나, onCompletion/catch 이후 실행 순서 주의
            }
        }
    }

    fun onScreenResumed() {
        Timber.d("MultiScreen resumed.")
        // 기존 검색 작업(SSE 연결 포함)이 활성 상태가 아니면 재시작
        if (searchJob == null || searchJob?.isActive == false) {
            Timber.i("SSE connection seems inactive. Restarting room loading.")
            // 현재 검색어와 페이지 인덱스로 다시 로드 시작
            loadRooms(uiState.value.roomSearchText, uiState.value.pageIndex)
        } else {
            Timber.d("SSE connection job is already active.")
        }
    }

    fun onScreenPaused() {
        Timber.d("MultiScreen paused.")
        // 화면이 일시 정지되면 검색 작업 취소
        cancelSearch()
    }


    private fun searchCourse(){
        viewModelScope.launch {
            getCourseUseCase().collect{ result ->
                result.onSuccess {
                    processIntent(MultiIntent.UpdateCourse(it))
                }
                result.onFailure {
                    // 에러 처리
                }
            }
        }
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