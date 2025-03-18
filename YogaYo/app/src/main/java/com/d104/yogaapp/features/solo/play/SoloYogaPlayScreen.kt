package com.d104.yogaapp.features.solo.play

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.GifImage
import com.d104.yogaapp.utils.PermissionChecker
import com.d104.yogaapp.features.common.RotateScreen
import com.d104.yogaapp.features.common.YogaPlayScreen


@Composable
fun SoloYogaPlayScreen(
    viewModel: SoloYogaPlayViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

//    // 화면 설정 (가로 모드, 전체 화면)
    RotateScreen(context)

    // 권한 요청 launcher를 여기서 정의 (Composable 함수 레벨)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.processIntent(SoloYogaPlayIntent.UpdateCameraPermission(isGranted))
    }

    // 카메라 권한 확인
    PermissionChecker.CheckPermission(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { isGranted ->
            // 권한 상태 변경 시 ViewModel에 알림
            viewModel.processIntent(SoloYogaPlayIntent.UpdateCameraPermission(isGranted))
        }
    )

    // 뒤로가기 처리
    BackHandler {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onBackPressed()
    }

    // 권한에 따른 UI 표시
    if (state.cameraPermissionGranted) {
        // 권한이 있는 경우 요가 플레이 화면 표시
        Box(modifier = Modifier.fillMaxSize()) {
            YogaPlayScreen(
                timerProgress = state.timerProgress,
                isPlaying = state.isPlaying,
                onPause = { viewModel.processIntent(SoloYogaPlayIntent.TogglePlayPause) },
                leftContent = {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 포즈 이름
                        Text(
                            text = state.currentPose.poseName,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        // GIF 콘텐츠
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 48.dp) // 제목 위 공간 확보
                        ) {
                            // GIF 표시 - isPlaying 상태 전달
                            GifImage(
                                url = state.currentPose.poseVideo,
                                modifier = Modifier.fillMaxSize(),
                                isPlaying = state.isPlaying
                            )
                        }
                    }
                }
            )

            // 일시정지 오버레이 표시
            if (!state.isPlaying) {
                PauseOverlay(
                    onResume = { viewModel.processIntent(SoloYogaPlayIntent.TogglePlayPause) },
                    onRestart = { viewModel.processIntent(SoloYogaPlayIntent.RestartCurrentPose) },
                    onSkip = {
                        viewModel.processIntent(SoloYogaPlayIntent.SkipPose)
                    },
                    onExit = {
                        viewModel.processIntent(SoloYogaPlayIntent.Exit)
                        val activity = context as? Activity
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        onBackPressed()
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

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 재생 버튼
            PauseActionButton(
                icon = R.drawable.ic_resume,
                text = "계속하기",
                onClick = onResume
            )

            // 다시 시작 버튼
            PauseActionButton(
                icon = R.drawable.ic_restart,
                text = "다시 시작",
                onClick = onRestart
            )

            PauseActionButton(
                icon = R.drawable.ic_skip,
                text = "건너뛰기",
                onClick = onSkip
            )

            // 나가기 버튼
            PauseActionButton(
                icon = R.drawable.ic_exit,
                text = "나가기",
                onClick = onExit
            )
        }
    }
}

@Composable
fun PauseActionButton(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

