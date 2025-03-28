package com.d104.yogaapp.features.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d104.domain.model.Badge
import com.d104.domain.model.BadgeDetail
import com.d104.domain.model.User
import com.d104.domain.model.UserRecord
import com.d104.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val myPageReducer: MyPageReducer,
    private val logoutUseCase: LogoutUseCase
): ViewModel(){
    private val _uiState = MutableStateFlow(MyPageState())
    val uiState : StateFlow<MyPageState> = _uiState.asStateFlow()


    // 뱃지 데이터 (목업)
    val tmpBadges = listOf(
        Badge(
            badgeId = 0,
            badgeName = "첫 코스0",
            badgeProgress = 4,
            highLevel = 0,
            badgeDetails = listOf(
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "1단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 10,
                    badgeLevel = 1,
                ),
                BadgeDetail(
                    badgeDetailId = 1,
                    badgeDetailName = "2단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 20,
                    badgeLevel = 2,
                ),
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "3단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 30,
                    badgeLevel = 3,
                )
            )
        ),
        Badge(
            badgeId = 1,
            badgeName = "첫 코스1",
            badgeProgress = 7,
            highLevel = 0,
            badgeDetails = listOf(
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "1단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 10,
                    badgeLevel = 1,
                ),
                BadgeDetail(
                    badgeDetailId = 1,
                    badgeDetailName = "2단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 20,
                    badgeLevel = 2,
                ),
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "3단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 30,
                    badgeLevel = 3,
                )
            )
        ),
        Badge(
            badgeId = 2,
            badgeName = "첫 코스2",
            badgeProgress = 2,
            highLevel = 0,
            badgeDetails = listOf(
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "1단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 10,
                    badgeLevel = 1,
                ),
                BadgeDetail(
                    badgeDetailId = 1,
                    badgeDetailName = "2단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 20,
                    badgeLevel = 2,
                ),
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "3단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 30,
                    badgeLevel = 3,
                )
            )
        ),
        Badge(
            badgeId = 3,
            badgeName = "첫 코스3",
            badgeProgress = 0,
            highLevel = 0,
            badgeDetails = listOf(
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "1단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 10,
                    badgeLevel = 1,
                ),
                BadgeDetail(
                    badgeDetailId = 1,
                    badgeDetailName = "2단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 20,
                    badgeLevel = 2,
                ),
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "3단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 30,
                    badgeLevel = 3,
                )
            )
        ),
        Badge(
            badgeId = 4,
            badgeName = "첫 코스4",
            badgeProgress = 0,
            highLevel = 0,
            badgeDetails = listOf(
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "1단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 10,
                    badgeLevel = 1,
                ),
                BadgeDetail(
                    badgeDetailId = 1,
                    badgeDetailName = "2단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 20,
                    badgeLevel = 2,
                ),
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "3단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 30,
                    badgeLevel = 3,
                )
            )
        ),
        Badge(
            badgeId = 5,
            badgeName = "첫 코스5",
            badgeProgress = 0,
            highLevel = 0,
            badgeDetails = listOf(
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "1단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 10,
                    badgeLevel = 1,
                ),
                BadgeDetail(
                    badgeDetailId = 1,
                    badgeDetailName = "2단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 20,
                    badgeLevel = 2,
                ),
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "3단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 30,
                    badgeLevel = 3,
                )
            )
        ),
        Badge(
            badgeId = 6,
            badgeName = "첫 코스6",
            badgeProgress = 4,
            highLevel = 0,
            badgeDetails = listOf(
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "1단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 10,
                    badgeLevel = 1,
                ),
                BadgeDetail(
                    badgeDetailId = 1,
                    badgeDetailName = "2단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 20,
                    badgeLevel = 2,
                ),
                BadgeDetail(
                    badgeDetailId = 0,
                    badgeDetailName = "3단계",
                    badgeDetailImg = "",
                    badgeDescription = "",
                    badgeGoal = 30,
                    badgeLevel = 3,
                )
            )
        ),



    )

    init{
        //추후 서버에서 가져온 데이터로
        _uiState.update {
            it.copy(
                myBadgeList = tmpBadges,
                userRecord = UserRecord(
                    userId = -1,
                    userName = "RedLaw",
                    userNickName = "RedLaw",
                    userProfile = "",
                    exDays = 3,
                    exConDays = 3,
                    roomWin = 1
                )
            )
        }
    }

    fun processIntent(intent: MyPageIntent){
        val newState = myPageReducer.reduce(_uiState.value,intent)
        _uiState.value = newState
        when(intent){
            is MyPageIntent.Logout -> {
                performLogout()
            }
            is MyPageIntent.LogoutSuccess -> {

            }
        }
    }

    private fun performLogout(){
        _uiState.value = _uiState.value.copy(isLoading = true)
        processIntent(MyPageIntent.LogoutSuccess)
        viewModelScope.launch{

            logoutUseCase()
                .collect{ result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if(result){
                        processIntent(MyPageIntent.LogoutSuccess)
                    }
                }
        }
    }
}