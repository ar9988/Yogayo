package com.d104.yogaapp.features.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.Badge
import com.d104.domain.model.BadgeDetail
import com.d104.domain.usecase.GetMyBadgeUseCase
import com.d104.domain.usecase.GetMyPageInfoUseCase
import com.d104.domain.usecase.GetNewBadgesUseCase
import com.d104.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val myPageReducer: MyPageReducer,
    private val logoutUseCase: LogoutUseCase,
    private val getMyPageInfoUseCase: GetMyPageInfoUseCase,
    private val getMyBadgeUseCase: GetMyBadgeUseCase,
    private val getMyNewBadgesUseCase: GetNewBadgesUseCase
): ViewModel(){
    private val _uiState = MutableStateFlow(MyPageState())
    val uiState : StateFlow<MyPageState> = _uiState.asStateFlow()

    private var myPageInfoLoaded = false
    private var myBadgesLoaded = false
    private var myNewBadgesLoaded = false

    fun handleIntent(intent: MyPageIntent){
        val newState = myPageReducer.reduce(_uiState.value,intent)
        _uiState.value = newState
        when(intent){
            is MyPageIntent.Logout -> {
                performLogout()
            }
            is MyPageIntent.LogoutSuccess -> {

            }

            MyPageIntent.Initialize -> {
                myPageInfoLoaded = false
                myBadgesLoaded = false
                myNewBadgesLoaded = false
                getMyPageUserInfo()
                getMyBadges()
                getNewBadges()
            }
            is MyPageIntent.SetMyPageInfo -> {
                myPageInfoLoaded = true
                checkAndUpdateLoadingState()
            }

            is MyPageIntent.SetMyBadges -> {
                myBadgesLoaded = true
                checkAndUpdateLoadingState()
            }
            else ->{

            }
        }
    }

    private fun performLogout(){
        _uiState.value = _uiState.value.copy(isLoading = true)
        handleIntent(MyPageIntent.LogoutSuccess)
        viewModelScope.launch{

            logoutUseCase()
                .collect{ result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if(result){
                        handleIntent(MyPageIntent.LogoutSuccess)
                    }
                }
        }
    }

    private fun getMyPageUserInfo(){
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                getMyPageInfoUseCase().collect { result ->
                    when {
                        result.isSuccess -> {
                            val myPageInfo = result.getOrNull()
                            myPageInfo?.let {
                                handleIntent(MyPageIntent.SetMyPageInfo(it))
                            }
                        }

                        result.isFailure -> {
                            myPageInfoLoaded = true
                            Timber.e(result.exceptionOrNull())
                            checkAndUpdateLoadingState()
                        }
                    }
                }
            }catch (e:Exception){
                myPageInfoLoaded = true
                Timber.e("Failed to fetch best pose histories", e)
                checkAndUpdateLoadingState()
            }
        }

    }

    private fun getMyBadges() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                getMyBadgeUseCase().collect { result ->
                    when {
                        result.isSuccess -> {
                            val myPageInfo = result.getOrNull()
                            myPageInfo?.let {
                                Timber.d("badge${it}")
                                handleIntent(MyPageIntent.SetMyBadges(it))
                            }
                        }

                        result.isFailure -> {
                            myBadgesLoaded = true
                            Timber.e(result.exceptionOrNull())
                            checkAndUpdateLoadingState()
                        }
                    }
                }
            } catch (e: Exception) {
                myBadgesLoaded = true
                Timber.e("Failed to fetch best pose histories", e)
                checkAndUpdateLoadingState()
            }
        }
    }

    private fun getNewBadges() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                getMyNewBadgesUseCase().collect { result ->
                    when {
                        result.isSuccess -> {
                            val myNewBadges = result.getOrNull()
                            myNewBadges?.let {
                                Timber.d("Newbadge${it}")
                                handleIntent(MyPageIntent.SetNewBadges(it))
                            }
                        }

                        result.isFailure -> {
                            Timber.e(result.exceptionOrNull())
                        }
                    }
                    myNewBadgesLoaded = true
                    checkAndUpdateLoadingState()
                }
            } catch (e: Exception) {
                myNewBadgesLoaded = true
                Timber.e("Failed to fetch best pose histories", e)
                checkAndUpdateLoadingState()
            }
        }
    }

    fun initalize(){
        handleIntent(MyPageIntent.Initialize)
    }
    fun logout(){
        handleIntent(MyPageIntent.Logout)
    }

    fun showNextBadge() {
        handleIntent(MyPageIntent.ShowNextBadge)
    }

    fun closeBadgeOverlay() {
        handleIntent(MyPageIntent.CloseBadgeOverlay)
    }

    private fun checkAndUpdateLoadingState() {
        if (myPageInfoLoaded && myBadgesLoaded&&myNewBadgesLoaded) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}