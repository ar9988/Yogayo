package com.d104.yogaapp.features.mypage.posehistory

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.usecase.GetYogaPoseHistoryDetailUseCase
import com.d104.yogaapp.features.solo.play.DownloadState
import com.d104.yogaapp.utils.ImageDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PoseHistoryViewModel @Inject constructor(
    private val reducer: PoseHistoryReducer,
    savedStateHandle: SavedStateHandle, // Navigation Argument 받기 위해 필요
    private val imageDownloader: ImageDownloader,
    private val getYogaPoseHistoryDetailUseCase: GetYogaPoseHistoryDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PoseHistoryState())
    val state: StateFlow<PoseHistoryState> = _state.asStateFlow()

    // Navigation Argument에서 poseId 추출
    private val poseId: Long = savedStateHandle.get<Long>("poseId") ?: -1L

    init {
        if (poseId != -1L) {
            processIntent(PoseHistoryIntent.Initialize(poseId))
        } else {
            processIntent(PoseHistoryIntent.ShowError("포즈 정보를 불러올 수 없습니다."))
        }
    }

    fun processIntent(intent: PoseHistoryIntent) {
        // Reducer를 통해 상태 업데이트 (SetPoseHistoryData 제외하고 먼저 업데이트 가능)
        if (intent !is PoseHistoryIntent.SetPoseHistoryData) {
            _state.value = reducer.reduce(state.value, intent)
        }

        when (intent) {
            is PoseHistoryIntent.Initialize -> {
                fetchPoseHistoryData(intent.poseId)
            }
            is PoseHistoryIntent.SetPoseHistoryData -> {
                // 데이터 로딩 완료 후 Reducer로 상태 최종 업데이트
                _state.value = reducer.reduce(state.value, intent)
            }
            is PoseHistoryIntent.SetLoading -> {
                // 상태는 위에서 이미 업데이트 됨
            }
            is PoseHistoryIntent.ShowError -> {
                // 상태는 위에서 이미 업데이트 됨
            }
            else->{

            }
        }
    }

    private fun fetchPoseHistoryData(poseId: Long) {
        viewModelScope.launch {
            try {

                getYogaPoseHistoryDetailUseCase(poseId).collect{result->
                    Timber.d("PoseHistoryDetail: ${result}")
                    when{
                        result.isSuccess->{
                            val yogaPoseHistoryDetail = result.getOrNull()
                            yogaPoseHistoryDetail?.let{
                                processIntent(PoseHistoryIntent.SetPoseHistoryData(it))
                            }
                        }
                        result.isFailure->{
                            Timber.e(result.exceptionOrNull())
                        }

                    }

                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch pose history for poseId: $poseId")
                processIntent(PoseHistoryIntent.ShowError("기록을 불러오는 데 실패했습니다: ${e.message}"))
            }
        }
    }

    fun downloadImage(imageUri: Uri, poseName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(downloadState = DownloadState.Loading)
            try {
                val success = imageDownloader.saveImageToGallery(imageUri.toString(), poseName)
                _state.value = if (success) _state.value.copy(downloadState = DownloadState.Loading) else _state.value.copy(downloadState = DownloadState.Error("저장 실패"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(downloadState = DownloadState.Error("저장 실패"))
            }
        }
    }
}