package com.d104.yogaapp.features.solo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.yogaapp.features.common.GifImage
import com.d104.yogaapp.features.common.YogaPlayScreen


@Composable
fun SoloScreen(onNavigateToYogaPlay: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onNavigateToYogaPlay) {
                Text("요가 카메라")
            }
        }
    }

}


@Composable
fun SoloYogaPlayScreen(
    viewModel: SoloYogaViewModel = hiltViewModel(),
    onBackPressed: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current


    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // 상단바와 네비게이션 바 숨기기
        WindowCompat.setDecorFitsSystemWindows(activity?.window ?: return@DisposableEffect onDispose {}, false)

        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)

            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            // 추가: 전체 화면 플래그 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        onDispose {
            val act = context.findActivity()
            act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // 시스템 바 및 화면 모드 복원
            act?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, true)

                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.statusBars())
                    show(WindowInsetsCompat.Type.navigationBars())
                }

                // 플래그 제거
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }
        }
    }


    // 뒤로가기 처리
    BackHandler {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onBackPressed()
    }


    YogaPlayScreen(
        timerProgress = state.timerProgress,
        isPlaying = state.isPlaying,
        onPause = { viewModel.processIntent(SoloYogaPlayIntent.SkipPose) },
        leftContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    // GIF 표시
                    GifImage(
                        poseVideo = state.currentPose.poseVideo,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
    )
}

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}