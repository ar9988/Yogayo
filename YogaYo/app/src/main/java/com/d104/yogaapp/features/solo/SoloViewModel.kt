package com.d104.yogaapp.features.solo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.usecase.CreateCourseUseCase
import com.d104.domain.usecase.DeleteCourseUseCase
import com.d104.domain.usecase.GetCourseUseCase
import com.d104.domain.usecase.UpdateCourseUseCase
import com.d104.yogaapp.utils.CourseJsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SoloViewModel @Inject constructor(
    private val courseJsonParser: CourseJsonParser,
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val getCourseUseCase: GetCourseUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase
): ViewModel(){

    private val _state = MutableStateFlow(SoloState(isLoading = true))
    val state: StateFlow<SoloState> = _state.asStateFlow()

    private lateinit var defaultCourse : List<UserCourse>
    
    
    //임시 포즈 정보들 추후 db연결 후 삭제
    val tmpPoseInfo = listOf(
        YogaPose(1, "나무 자세", "https://d5sbbf6usl3xq.cloudfront.net/baddhakonasana.png", 1, "나무 자세 설명", "video_url", -1),
        YogaPose(2, "나무 자세", "https://d5sbbf6usl3xq.cloudfront.net/utthita_trikonasana_flip.png", 3, "나무 자세 설명", "video_url", 3),
        YogaPose(3, "전사 자세", "https://d5sbbf6usl3xq.cloudfront.net/utthita_parsvakonasana.png", 2, "전사 자세 설명", "video_url", 2),
        YogaPose(4, "다운독 자세", "https://d5sbbf6usl3xq.cloudfront.net/samasthiti.png", 2, "다운독 자세 설명", "video_url", -1)
    )

    init {
        handleIntent(SoloIntent.LoadCourses)
    }

    // Intent 처리
    fun handleIntent(intent: SoloIntent) {
        when (intent) {
            is SoloIntent.LoadCourses -> loadCourses()
            is SoloIntent.CreateCourse -> createCourse(intent.courseName, intent.poses)
            is SoloIntent.DeleteCourse -> deleteCourse(intent.courseId)
            is SoloIntent.UpdateCourse -> updateCourse(intent.courseId,intent.courseName,intent.poses)
        }
    }

    // 코스 데이터 로드
    private fun loadCourses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                defaultCourse = courseJsonParser.loadUserCoursesFromAssets("courseSet.json")
                _state.update {
                    it.copy(
                        courses = defaultCourse,
                        isLoading = false,
                        error = null
                    )
                }


            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "코스를 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    private fun createCourse(courseName: String, poses: List<YogaPoseWithOrder>) {
        viewModelScope.launch {
            try {
                // 코스 생성 UseCase 호출
//                val a = createCourseUseCase(courseName,poses)
                //responsebody로 받은 걸 추가
                _state.update {
                    it.copy(courses = it.courses + UserCourse(courseId = 1, courseName = "유저 코스",tutorial = true, poses = tmpPoseInfo))
                }
                Timber.d("${_state.value.courses}")
//                val newCourse = UserCourse(
//                    id = System.currentTimeMillis(), // 임시 ID 생성 방식
//                    name = courseName,
//                    poses = poses,
//                    // 기타 필요한 속성들...
//                )
//
//                createCourseUseCase(newCourse)
//
//                // 상태 업데이트 - 새 코스 목록 불러오기 또는 현재 목록에 추가
//                val updatedCourses = _state.value.courses.toMutableList()
//                updatedCourses.add(newCourse)
//
//                _state.update {
//                    it.copy(
//                        courses = updatedCourses,
//                        error = null
//                    )
//                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "코스 생성에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    private fun deleteCourse(courseId:Long){

    }
    private fun updateCourse(courseId:Long, courseName:String, poses:List<YogaPoseWithOrder>){

    }

}