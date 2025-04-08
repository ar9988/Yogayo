package com.d104.yogaapp.features.solo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.usecase.CreateCourseUseCase
import com.d104.domain.usecase.DeleteCourseUseCase
import com.d104.domain.usecase.GetUserCourseUseCase
import com.d104.domain.usecase.GetLoginStatusUseCase
import com.d104.domain.usecase.GetYogaPosesUseCase
import com.d104.domain.usecase.UpdateCourseUseCase
import com.d104.yogaapp.utils.CourseJsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SoloViewModel @Inject constructor(
    private val reducer:SoloReducer,
    private val courseJsonParser: CourseJsonParser,
    private val getLoginStatusUseCase:GetLoginStatusUseCase,
    private val getYogaPosesUseCase: GetYogaPosesUseCase,
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val getUserCourseUseCase: GetUserCourseUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase

): ViewModel(){

    private val _state = MutableStateFlow(SoloState(isLoading = true))
    val state: StateFlow<SoloState> = _state.asStateFlow()

    private var defaultCourse : List<UserCourse> = emptyList()
    private var isDefaultCourseLoaded = false



    init {
        // 병렬로 초기 데이터 로드
        courseJsonParser.printModelMetadata()
        viewModelScope.launch {
            try {
                // 기본 코스와 포즈 데이터를 병렬로 로드
                val courseDeferred = viewModelScope.async(Dispatchers.IO) {
                    courseJsonParser.loadUserCoursesFromAssets("courseSet.json")
                }
                val loginStatusDeferred = viewModelScope.async {
                    getLoginStatusUseCase().first()
                }

                // 결과 대기 및 적용
                defaultCourse = courseDeferred.await()
                val isLogin = loginStatusDeferred.await()
                isDefaultCourseLoaded = true

                // 상태 업데이트는 한 번만
                _state.update {
                    it.copy(
                        isLogin = isLogin,
                        isLoading = false,
                        courses = if (isLogin) emptyList() else defaultCourse
                    )
                }

                // 로그인된 경우에만 사용자 코스 로드
                if (isLogin) {
                    loadCourses()
                }

                // 포즈 데이터는 필요할 때만 로드
                if(_state.value.yogaPoses.isEmpty()) {
                    getYogaPoses()
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "기본 코스를 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
        viewModelScope.launch {
            getLoginStatusUseCase().collectLatest { isLogin ->
                _state.update { it.copy(isLogin = isLogin) }
                if (isDefaultCourseLoaded) { // 기본 코스가 로드된 후에만 loadCourses() 호출
                    loadCourses()
                }
            }
        }
    }

    // Intent 처리
    fun handleIntent(intent: SoloIntent) {
        val newState = reducer.reduce(state.value, intent)

        // 상태 업데이트
        _state.value = newState
        when (intent) {
            is SoloIntent.LoadCourses -> loadCourses()
            is SoloIntent.CreateCourse -> createCourse(intent.courseName, intent.poses)
            is SoloIntent.DeleteCourse -> deleteCourse(intent.courseId)
            is SoloIntent.UpdateCourse -> updateCourse(intent.courseId,intent.courseName,intent.poses)
            is SoloIntent.UpdateCourseTutorial -> updateCourseTutorial(intent.course, intent.tutorial)
            is SoloIntent.ShowAddCourseDialog-> getYogaPoses()
            else->{}

        }
    }



    // 코스 데이터 로드
    private fun loadCourses() {
        if (!isDefaultCourseLoaded) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                if (_state.value.isLogin) {
                    // 로그인 상태
                    getUserCourseUseCase().collect { result ->
                        when {
                            result.isSuccess -> {
                                val userCourses = result.getOrDefault(emptyList())
                                _state.update {
                                    it.copy(
                                        courses = defaultCourse + userCourses,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }
                            result.isFailure -> {
                                _state.update {
                                    it.copy(
                                        courses = defaultCourse,
                                        error = result.exceptionOrNull()?.message ?: "Unknown error",
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 비로그인 상태
                    _state.update {
                        it.copy(
                            courses = defaultCourse,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        courses = defaultCourse,
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun createCourse(courseName: String, poses: List<YogaPoseWithOrder>) {
        viewModelScope.launch {
            try {
                createCourseUseCase(courseName,poses).collect{ result->
                    when {
                        result.isSuccess -> {
                            val userCourse = result.getOrNull()
                            userCourse?.let {userCoursesResult->
                                _state.update {
                                    it.copy(
                                        courses = it.courses + userCoursesResult,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }
                        }
                        result.isFailure -> {
                            Timber.d("Error: ${result.exceptionOrNull()}")
                            _state.update {
                                it.copy(
                                    courses = defaultCourse,
                                    error = result.exceptionOrNull()?.message ?: "Unknown error",
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
                Timber.d("${_state.value.courses}")

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "코스 생성에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            _state.update {
                it.copy(courses = it.courses.filter { course -> course.courseId != courseId })
            }
            deleteCourseUseCase(courseId).collectLatest {result->
                when{
                    result.isFailure->{
                        loadCourses()
                    }
                }
            }
        }
    }
    private fun updateCourse(courseId:Long, courseName:String, poses:List<YogaPoseWithOrder>){
        viewModelScope.launch {
            updateCourseUseCase(courseId,courseName,poses).collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val modifiedCourse = result.getOrNull()
                        modifiedCourse?.let { updatedCourse ->
                            _state.update { currentState ->
                                // 기존 코스 목록에서 업데이트된 코스로 교체
                                val updatedCourses = currentState.courses.map { course ->
                                    if (course.courseId == courseId) updatedCourse else course
                                }
                                currentState.copy(courses = updatedCourses)
                            }
                        }
                    }

                    result.isFailure -> {
                        Timber.e("${result.exceptionOrNull()?.message}")
                    }

                }
            }
        }
    }

    private fun updateCourseTutorial(course: UserCourse, tutorial: Boolean) {
        viewModelScope.launch {
            val posesWithOrder = course.poses.mapIndexed{index, yogaPose->
                YogaPoseWithOrder(yogaPose.poseId,index)
            }
            updateCourseUseCase(course.courseId,course.courseName,posesWithOrder,tutorial).collectLatest { result ->
                when {
                    result.isSuccess -> {
                        val modifiedCourse = result.getOrNull()
                        modifiedCourse?.let { updatedCourse ->
                            _state.update { currentState ->
                                // 기존 코스 목록에서 업데이트된 코스로 교체
                                val updatedCourses = currentState.courses.map { course ->
                                    if (course.courseId == updatedCourse.courseId) updatedCourse else course
                                }
                                currentState.copy(courses = updatedCourses)
                            }
                        }
                    }

                    result.isFailure -> {
                        Timber.e("${result.exceptionOrNull()?.message}")
                    }

                }
            }
        }
    }

    private fun getYogaPoses(){
        if (_state.value.yogaPoses.isEmpty()) {
            viewModelScope.launch {
                try {
                    getYogaPosesUseCase().collectLatest { result ->
                        when {
                            result.isSuccess -> {
                                _state.update {
                                    it.copy(
                                        yogaPoses = result.getOrDefault(emptyList()),
                                        yogaPoseLoading = false
                                    )
                                }
                            }
                            result.isFailure -> {
                                Timber.d("Error: ${result.exceptionOrNull()}")
                                _state.update {
                                    it.copy(
                                        error = result.exceptionOrNull()?.message ?: "Unknown error",
                                        yogaPoseLoading = false
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching yoga poses")
                    _state.update {
                        it.copy(
                            error = e.message ?: "Unknown error",
                            yogaPoseLoading = false
                        )
                    }
                }
            }
        }else{
            _state.update { it.copy(yogaPoseLoading = false) }
        }
    }



}