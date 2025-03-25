package com.d104.yogaapp.features.multi.play

import com.d104.yogaapp.features.solo.play.SoloYogaPlayIntent
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
import com.d104.yogaapp.features.common.YogaAnimationScreen
import com.d104.yogaapp.features.common.YogaPlayScreen


@Composable
fun MultiPlayScreen(
    viewModel: MultiPlayViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

//    // 화면 설정 (가로 모드, 전체 화면)
    RotateScreen(context)
//
//    // 권한 요청 launcher를 여기서 정의 (Composable 함수 레벨)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.processIntent(MultiPlayIntent.UpdateCameraPermission(isGranted))
    }
//
    // 카메라 권한 확인
    PermissionChecker.CheckPermission(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { isGranted ->
            // 권한 상태 변경 시 ViewModel에 알림
            viewModel.processIntent(MultiPlayIntent.UpdateCameraPermission(isGranted))
        }
    )
//
//    // 뒤로가기 처리
    BackHandler {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onBackPressed()
    }
//
//    // 권한에 따른 UI 표시
    if (uiState.cameraPermissionGranted) {
        // 권한이 있는 경우 요가 플레이 화면 표시
        Box(modifier = Modifier.fillMaxSize()) {
            YogaPlayScreen(
                isMultiPlay = true,
                timerProgress = uiState.timerProgress,
                isPlaying = uiState.isPlaying,
                onPause = { viewModel.processIntent(MultiPlayIntent.TogglePlayPause) },
                leftContent = {
                    YogaAnimationScreen(
                        pose = uiState.currentPose,
                        accuracy = uiState.currentAccuracy,
                        isPlaying = uiState.isPlaying
                    )
                },
                onImageCaptured = { bitmap ->
                    viewModel.processIntent(MultiPlayIntent.CaptureImage(bitmap))
                }
                )

            // 일시정지 오버레이 표시
            if (!uiState.menuClicked) {
                MenuOverlay(
                    onResume = { viewModel.processIntent(MultiPlayIntent.ClickMenu) },
                    onExit = {
                        viewModel.processIntent(MultiPlayIntent.ExitRoom)
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
fun MenuOverlay(
    onResume: () -> Unit,
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
            // 계속하기 버튼
            PauseActionButton(
                icon = R.drawable.ic_resume,
                text = "계속하기",
                onClick = onResume
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

