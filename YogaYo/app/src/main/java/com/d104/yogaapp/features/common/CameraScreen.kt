package com.d104.yogaapp.features.common

import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.d104.yogaapp.R
import com.google.firebase.perf.util.Timer
import timber.log.Timber
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.RectangleShape


@Composable
fun YogaPlayScreen(
    timerProgress: Float,
    isPlaying: Boolean,
    onPause: () -> Unit,
    leftContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 메인 콘텐츠 영역 (가로 배치)
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // 왼쪽 콘텐츠 (GIF) - 40%
            Box(
                modifier = Modifier
                    .weight(0.38f)
                    .background(Color.White)
                    .paint(
                        painterResource(id = R.drawable.bg_double_border),
                        contentScale = ContentScale.FillBounds
                    )
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                    leftContent()
            }
            Spacer(modifier = Modifier
                .weight(0.02f))

            // 오른쪽 콘텐츠 (카메라) - 60%
            Box(
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight()
            ) {
                // 카메라 프리뷰
                CameraPreview(modifier = Modifier.fillMaxSize())

                // 일시정지 버튼
                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = if (isPlaying) "일시정지" else "재생",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // 타이머 프로그레스 바
                    LinearProgressIndicator(
                        progress = { timerProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(36.dp)),
                        color = Color(0xFF2196F3),
                        trackColor = Color(0x80FFFFFF) // 반투명 흰색
                    )

                    // 타이머 아이콘 (프로그레스 바 위에 겹치게)
                    Image(
                        painter = painterResource(id = R.drawable.img_timer),
                        contentDescription = "타이머",
                        modifier = Modifier
                            .size(54.dp)
                            .offset(x = (-6).dp,y=-4.dp), // 약간 왼쪽으로 이동
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    //권한 요청은 camera view말고 각 플레이 페이지 진입 지점으로 이동할듯
    // 권한 요청 런처
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
    }

    // 권한 상태 확인 및 즉시 요청
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                cameraPermissionGranted = true
            }
            else -> {
                // 즉시 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Box(modifier = modifier) {
        if (cameraPermissionGranted) {
            // 카메라 프리뷰 표시
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            Timber.e("카메라 바인딩 실패", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("카메라 권한이 필요합니다")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("권한 요청")
                }
            }
        }
    }
}