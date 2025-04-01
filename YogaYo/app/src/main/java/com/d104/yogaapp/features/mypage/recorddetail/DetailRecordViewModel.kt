package com.d104.yogaapp.features.mypage.recorddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.BestPoseRecord
import com.d104.domain.usecase.GetBestPoseRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DetailRecordViewModel @Inject constructor(
    private val reducer: DetailRecordReducer,
    private val getBestPoseRecordsUseCase: GetBestPoseRecordsUseCase
): ViewModel() {
    private val _state = MutableStateFlow(DetailRecordState())
    val state : StateFlow<DetailRecordState> = _state.asStateFlow()


    fun handleIntent(intent: DetailRecordIntent) {
        val newState = reducer.reduce(state.value, intent)

        // 상태 업데이트
        _state.value = newState
        when (intent) {
            is DetailRecordIntent.initialize -> {
                fetchBestPoseHistories()
            }
            is DetailRecordIntent.SetUserRecord -> {}
            is DetailRecordIntent.SetBestPoseHistories -> {}

        }
    }

    private fun fetchBestPoseHistories() {
        // ViewModel의 CoroutineScope 사용
        viewModelScope.launch {
            try {

                getBestPoseRecordsUseCase().collect {result->
                    when{
                        result.isSuccess ->{
                            val bestPoseHistories = result.getOrDefault(emptyList())
                            handleIntent(DetailRecordIntent.SetBestPoseHistories(bestPoseHistories))
                        }
                        result.isFailure ->{
                            Timber.e(result.exceptionOrNull())
                        }
                    }
                }
            } catch (e: Exception) {
                // 에러 처리
                Timber.e("Failed to fetch best pose histories", e)
                // 에러 상태를 업데이트하는 Intent 전달 (예시)
                _state.value = state.value.copy(isLoading = false) // 직접 상태 업데이트도 가능
            }
        }
    }


}