package com.d104.yogaapp.features.multi.play

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.domain.model.Room
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.RotateScreen
import com.d104.yogaapp.features.common.YogaAnimationScreen
import com.d104.yogaapp.features.multi.play.components.MenuOverlay
import com.d104.yogaapp.features.multi.play.components.MultiYogaPlayScreen
import com.d104.yogaapp.features.multi.play.components.RoundResultScreen
import com.d104.yogaapp.features.multi.play.components.WaitingScreen
import com.d104.yogaapp.features.multi.play.result.DetailScreen
import com.d104.yogaapp.features.multi.play.result.GalleryScreen
import com.d104.yogaapp.features.multi.play.result.LeaderboardScreen
import timber.log.Timber


@Composable
fun MultiPlayScreen(
    viewModel: MultiPlayViewModel = hiltViewModel(),
    room: Room,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.exit) {
        if(uiState.exit){
            val activity = context as? Activity
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            onBackPressed()
        }
    }

    LaunchedEffect(room) {
        // Check prevents re-initializing if already set (e.g., during recomposition)
        if (uiState.currentRoom == null || uiState.currentRoom?.roomId != room.roomId) {
            viewModel.processIntent(MultiPlayIntent.InitializeRoom(room))
        }
    }
//    // 화면 설정 (가로 모드, 전체 화면)
    if (uiState.gameState == GameState.Playing || uiState.gameState == GameState.Waiting || uiState.gameState == GameState.RoundResult) {
        // 요가 플레이 중이거나 가이드 중일 때만 가로 모드로 설정
        RotateScreen(context)
    }
//    // 권한 요청 launcher를 여기서 정의 (Composable 함수 레벨)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.processIntent(MultiPlayIntent.UpdateCameraPermission(isGranted))
    }
    // 권한 체크
    LaunchedEffect(key1 = Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.processIntent(MultiPlayIntent.UpdateCameraPermission(true))
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
//
//    // 뒤로가기 처리
    BackHandler {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        if(uiState.gameState == GameState.Waiting){
            viewModel.processIntent(MultiPlayIntent.ExitRoom)
        } else if (uiState.gameState == GameState.Playing||uiState.gameState == GameState.RoundResult){
            viewModel.processIntent(MultiPlayIntent.ClickMenu)
        } else if (uiState.gameState == GameState.GameResult){
            onBackPressed()
        }
        else {
            viewModel.processIntent(MultiPlayIntent.BackPressed)
        }
    }
    if (uiState.gameState == GameState.GameResult) {
        LeaderboardScreen(
            userList = uiState.userList,
            onNextClick = {
            viewModel.processIntent(MultiPlayIntent.ClickNext)
        })
    } else if (uiState.gameState == GameState.Gallery) {
        GalleryScreen(
            onItemClick = {
                viewModel.processIntent(MultiPlayIntent.ClickPose(it))
            },
            onCheckClick = {
                onBackPressed()
            },
            bestUrls = uiState.bestUrls,
            processIntent = {
                viewModel.processIntent(MultiPlayIntent.ClickPhoto(it))
            }
        )
    } else if (uiState.gameState == GameState.Detail){
        DetailScreen(
            onBackButtonClick = {
                viewModel.processIntent(MultiPlayIntent.BackPressed)
            },
            poseList = uiState.currentRoom!!.userCourse.poses,
            selectedPoseId = uiState.selectedPoseId,
            allUrls = uiState.allUrls,
        )
    }
    // 권한에 따른 UI 표시
    else if (uiState.cameraPermissionGranted) {
        // 권한이 있는 경우 요가 플레이 화면 표시
        Box(modifier = Modifier.fillMaxSize()) {
            MultiYogaPlayScreen(
                gameState = uiState.gameState,
                timerProgress = uiState.timerProgress,
                isPlaying = uiState.isPlaying,
                isMenuClicked = uiState.menuClicked,
                onPause = { viewModel.processIntent(MultiPlayIntent.ClickMenu) },
                leftContent = {
                    when (uiState.gameState) {
                        GameState.Waiting -> {
                            WaitingScreen(
                                myId = uiState.myId,
                                userList = uiState.userList,
                                onReadyClick = { viewModel.processIntent(MultiPlayIntent.ReadyClick) }
                            )
                        }

                        GameState.Playing -> {
                            YogaAnimationScreen(
                                pose = uiState.currentPose,
                                accuracy = uiState.currentAccuracy,
                                isPlaying = uiState.isPlaying
                            )
                        }

                        GameState.RoundResult -> {

                            val bitmapToShow = if (uiState.source) {
                                uiState.bitmap
                            } else {
                                uiState.bestBitmap
                            }

                            RoundResultScreen(
                                isLoading = uiState.isLoading, // 로딩 상태는 그대로 유지
                                resultBitmap = bitmapToShow, // 조건부로 선택된 비트맵 전달
                                contentDescription = uiState.currentPose.poseName // 컨텐츠 설명도 유지
                            )
                        }

                        else -> {}
                    }
                },
                onSendResult = {pose,accuracy, time, bitmap: Bitmap ->  viewModel.processIntent(
                    MultiPlayIntent.SendHistory(pose,accuracy, time, bitmap))},
                userList = uiState.userList,
                pose = uiState.currentPose,
                onAccuracyUpdate = {accuracy,time->
                    Timber.d("multi accuracy: ${accuracy}")
                    viewModel.processIntent(MultiPlayIntent.SetCurrentHistory(accuracy,time))
                }
            )

            // 일시정지 오버레이 표시
            if (uiState.menuClicked) {
                MenuOverlay(
                    onResume = { viewModel.processIntent(MultiPlayIntent.ClickMenu) },
                    onExit = {
                        viewModel.processIntent(MultiPlayIntent.ExitRoom)
                    }
                )
            }
        }
    } else {
        // 권한이 없는 경우 권한 요청 UI 표시
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "카메라 권한이 필요합니다",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "요가 포즈를 분석하기 위해 카메라 접근 권한이 필요합니다.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("권한 요청")
                }
            }
        }
    }
}
