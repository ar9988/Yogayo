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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint



@Composable
fun YogaPlayScreen(
    timerProgress: Float,
    isPlaying: Boolean,
    onPause: () -> Unit,
    leftContent: @Composable () -> Unit,
    onImageCaptured: (Bitmap) -> Unit = {},
    isMultiPlay:Boolean = false
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
                    .weight(0.4f)
                    .background(Color.White)
                    .paint(
                        painterResource(id = R.drawable.bg_double_border),
                        contentScale = ContentScale.FillBounds
                    )
                    .fillMaxHeight()
                    .padding(end=5.dp,bottom=16.dp)
            ) {
                leftContent()
            }
            Spacer(modifier = Modifier
                .weight(0.02f))

            // 오른쪽 콘텐츠 (카메라) - 60%
            Box(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
            ) {
                // 카메라 프리뷰 - isPlaying 상태 전달
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = isPlaying,
                    onImageCaptured = onImageCaptured,
                    poseId = 1.toString(),
                    shouldCapture = if(timerProgress==0.5f) true else false
                )

                // 일시정지/재생 버튼
                if(isPlaying) {
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
                            contentDescription = "일시정지",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    poseId: String, // 포즈 식별자 (String으로 통일)
    onImageCaptured: (Bitmap) -> Unit, // 저장 완료 후 URI 전달
    shouldCapture: Boolean = false, // 외부에서 캡처 트리거
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // PreviewView 구성
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // 사진 캡처 함수
    val captureImage = remember(imageCapture, poseId) {
        {
            val imageCaptureUseCase = imageCapture
            if (imageCaptureUseCase != null) {
                imageCaptureUseCase.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            try {
                                // 이미지 프록시를 비트맵으로 변환
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                onImageCaptured(bitmap)

                            } catch (e: Exception) {
                                Timber.e("비트맵 변환 실패", e)
                            } finally {
                                image.close()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Timber.e("사진 캡처 실패", exception)
                        }
                    }
                )
            }
        }
    }

    // 바인딩 함수
    val bindCamera = remember {
        {
            try {
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Timber.e("카메라 바인딩 실패", e)
            }
        }
    }

    // 초기 카메라 설정
    DisposableEffect(Unit) {
        val cameraListener = Runnable {
            try {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                if (isPlaying) {
                    bindCamera()
                }
            } catch (e: Exception) {
                Timber.e("카메라 초기화 실패", e)
            }
        }

        cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    // isPlaying 상태 변경에 따른 처리
    LaunchedEffect(isPlaying) {
        if (cameraProvider != null && preview != null) {
            if (isPlaying) {
                bindCamera()
            } else {
                cameraProvider?.unbindAll()
            }
        }
    }

    // shouldCapture 상태가 true로 변경될 때 사진 캡처
    LaunchedEffect(shouldCapture) {
        if (shouldCapture) {
            captureImage()
        }
    }

    // 카메라 프리뷰 UI
    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

