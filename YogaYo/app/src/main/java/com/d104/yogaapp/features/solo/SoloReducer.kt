package com.d104.yogaapp.features.solo

import com.d104.yogaapp.features.solo.play.SoloYogaPlayIntent
import com.d104.yogaapp.features.solo.play.SoloYogaPlayState
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class SoloReducer@Inject constructor() {

    fun reduce(state: SoloState, intent: SoloIntent): SoloState {
        return when (intent) {
            is SoloIntent.ShowAddCourseDialog->{
                state.copy(
                    showAddCourseDialog = true,
                    yogaPoseLoading = true
                    )
            }
            is SoloIntent.HideAddCourseDialog -> {
                state.copy(showAddCourseDialog = false)
            }
            else->{state}
        }

    }
}