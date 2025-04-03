package com.d104.yogaapp.features.common

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.d104.yogaapp.R
import timber.log.Timber
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun YogaPlayScreen(
    timerProgress: Float,
    isPlaying: Boolean,
    onPause: () -> Unit,
    leftContent: @Composable () -> Unit,
    onImageCaptured: (Bitmap) -> Unit = {},
    isCountingDown: Boolean = false,
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
                    shouldCapture = if(timerProgress==0.5f) true else false,
                    isCountingDown = isCountingDown
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
//@Composable
//fun CameraPreview(
//    modifier: Modifier = Modifier,
//    isPlaying: Boolean = true,
//    poseId: String, // 포즈 식별자 (String으로 통일)
//    onImageCaptured: (Bitmap) -> Unit, // 저장 완료 후 URI 전달
//    shouldCapture: Boolean = false, // 외부에서 캡처 트리거
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
//    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
//    var preview by remember { mutableStateOf<Preview?>(null) }
//    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
//
//    // PreviewView 구성
//    val previewView = remember {
//        PreviewView(context).apply {
//            this.scaleType = PreviewView.ScaleType.FILL_CENTER
//            layoutParams = ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//            )
//            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//        }
//    }
//
//    // 사진 캡처 함수
//    val captureImage = remember(imageCapture, poseId) {
//        {
//            val imageCaptureUseCase = imageCapture
//            if (imageCaptureUseCase != null) {
//                imageCaptureUseCase.takePicture(
//                    ContextCompat.getMainExecutor(context),
//                    object : ImageCapture.OnImageCapturedCallback() {
//                        override fun onCaptureSuccess(image: ImageProxy) {
//                            try {
//                                // 이미지 프록시를 비트맵으로 변환
//                                val buffer = image.planes[0].buffer
//                                val bytes = ByteArray(buffer.remaining())
//                                buffer.get(bytes)
//                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//
//                                onImageCaptured(bitmap)
//
//                            } catch (e: Exception) {
//                                Timber.e("비트맵 변환 실패", e)
//                            } finally {
//                                image.close()
//                            }
//                        }
//
//                        override fun onError(exception: ImageCaptureException) {
//                            Timber.e("사진 캡처 실패", exception)
//                        }
//                    }
//                )
//            }
//        }
//    }
//
//    // 바인딩 함수
//    val bindCamera = remember {
//        {
//            try {
//                val cameraSelector = CameraSelector.Builder()
//                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
//                    .build()
//
//                imageCapture = ImageCapture.Builder()
//                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                    .build()
//
//                cameraProvider?.unbindAll()
//                cameraProvider?.bindToLifecycle(
//                    lifecycleOwner,
//                    cameraSelector,
//                    preview,
//                    imageCapture
//                )
//            } catch (e: Exception) {
//                Timber.e("카메라 바인딩 실패", e)
//            }
//        }
//    }
//
//    // 초기 카메라 설정
//    DisposableEffect(Unit) {
//        val cameraListener = Runnable {
//            try {
//                cameraProvider = cameraProviderFuture.get()
//                preview = Preview.Builder().build().also {
//                    it.setSurfaceProvider(previewView.surfaceProvider)
//                }
//
//                if (isPlaying) {
//                    bindCamera()
//                }
//            } catch (e: Exception) {
//                Timber.e("카메라 초기화 실패", e)
//            }
//        }
//
//        cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(context))
//
//        onDispose {
//            cameraProvider?.unbindAll()
//        }
//    }
//
//    // isPlaying 상태 변경에 따른 처리
//    LaunchedEffect(isPlaying) {
//        if (cameraProvider != null && preview != null) {
//            if (isPlaying) {
//                bindCamera()
//            } else {
//                cameraProvider?.unbindAll()
//            }
//        }
//    }
//
//    // shouldCapture 상태가 true로 변경될 때 사진 캡처
//    LaunchedEffect(shouldCapture) {
//        if (shouldCapture) {
//            captureImage()
//        }
//    }
//
//    // 카메라 프리뷰 UI
//    Box(modifier = modifier) {
//        AndroidView(
//            factory = { previewView },
//            modifier = Modifier.fillMaxSize()
//        )
//    }
//}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    poseId: String, // 필요 없다면 제거
    onImageCaptured: (Bitmap) -> Unit,
    shouldCapture: Boolean = false,
    viewModel: CameraViewModel = hiltViewModel(),
    isCountingDown: Boolean = false,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }

    // *** ViewModel의 Helper 준비 상태 관찰 ***
    val isHelperReady by viewModel.isHelperReady.collectAsState()
    // *** ViewModel의 결과 및 에러 관찰 (UI 표시에 사용) ***
    val poseResult by viewModel.poseResult.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    // PreviewView 구성 (변경 없음)
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
    // 사진 캡처 함수 (변경 없음, 단 Bitmap 변환 로직 정확성 확인 필요)
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

    // 바인딩 함수 (내부 조건 제거, isHelperReady로 외부에서 제어)
    val bindCamera = remember(viewModel.imageAnalyzer, cameraProvider, preview, imageCapture) {
        // imageAnalyzer 변경 외에도 provider, preview, capture 준비 시 재실행 필요할 수 있음
        {
            // isHelperReady 조건은 LaunchedEffect에서 처리하므로 여기선 제거
            if (!(cameraProvider == null || preview == null || imageCapture == null)) {

                try {
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()

                    // ImageAnalysis 설정 (Analyzer는 ViewModel에서 가져옴)
                    imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(640, 480)) // 해상도 확인
                        .setTargetRotation(previewView.display.rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            // *** ViewModel의 Analyzer 사용 ***
                            setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                viewModel.imageAnalyzer
                            ) // Executor 확인 필요
                            // 분석은 백그라운드 스레드 권장 -> cameraExecutor 직접 전달 불가
                            // viewModel.imageAnalyzer 내에서 스레드 관리 또는 별도 Executor 사용
                            // --> ViewModel의 cameraExecutor를 사용하도록 수정
                            // setAnalyzer(viewModel.cameraExecutor, viewModel.imageAnalyzer) // ViewModel에 cameraExecutor 노출 필요 시
                            // ---> 가장 간단한 방법: Analyzer 내에서 Helper 호출 시 백그라운드 처리 확인
                            // PoseLandmarkerHelper가 내부적으로 스레드를 관리하거나,
                            // ViewModel의 cameraExecutor에서 Helper 메소드 호출
                            // ViewModel의 Analyzer 구현에서 Helper 호출 시 스레드 고려.
                            // 현재 ViewModel 코드는 Analyzer 콜백 스레드에서 Helper 호출 중.
                            // PoseLandmarkerHelper의 detectAsync는 내부적으로 스레드 사용 예상.
                            // 여기서는 CameraX 기본 스레드 사용 가정.
                            // setAnalyzer(ContextCompat.getMainExecutor(context), viewModel.imageAnalyzer) // <- UI 스레드 사용 시 문제될 수 있음
                            // ViewModel에서 생성한 cameraExecutor를 사용하도록 수정
                            // CameraViewModel에 정의된 cameraExecutor 사용
                            setAnalyzer(viewModel.imageAnalyzerExecutor, viewModel.imageAnalyzer)


                        }

                    cameraProvider?.unbindAll()

                    Timber.d("Attempting to bind Preview, ImageCapture, ImageAnalysis...")
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis // ImageAnalysis 추가
                    )
                    Timber.d("Camera bound successfully.")
                } catch (e: Exception) {
                    Timber.e("카메라 바인딩 실패", e)
                    // ViewModel의 에러 상태 업데이트 또는 직접 처리
                    // viewModel.setError("Camera binding failed: ${e.message}")
                }
            }
        }
    }

    // 초기 카메라 설정 (변경 없음)
    DisposableEffect(Unit) {
        val cameraListener = Runnable {
            try {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                // imageCapture도 여기서 초기화하는 것이 좋음
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Provider, Preview, ImageCapture 준비 완료 후 바인딩 시도 (LaunchedEffect에서)
            } catch (e: Exception) {
                Timber.e("카메라 Provider/Preview/Capture 초기화 실패", e)
            }
        }
        cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
            // cameraExecutor는 ViewModel에서 관리하므로 여기서 shutdown 불필요
            Timber.d("CameraPreview disposed.")
        }
    }

    // *** isPlaying 및 isHelperReady 상태에 따른 처리 ***
    LaunchedEffect(isPlaying, isCountingDown,isHelperReady, cameraProvider, preview, imageCapture) {
        if (cameraProvider != null && preview != null && imageCapture != null) { // 모든 UseCase 준비 확인
            if (isPlaying && isHelperReady&&!isCountingDown) { // 재생 중이고 Helper 준비 완료 시
                Timber.d("isPlaying is true and Helper is ready. Binding camera with ImageAnalysis.")
                bindCamera() // 카메라 바인딩 (ImageAnalysis 포함)
            } else if (isPlaying && !isHelperReady) { // 재생 중이지만 Helper 준비 안됨
                Timber.d("isPlaying is true, but waiting for PoseLandmarkerHelper to be ready...")
                // 필요 시 로딩 표시
                cameraProvider?.unbindAll() // 분석 없이 프리뷰만 보려면 Preview만 바인딩
//                try {
//                    cameraProvider?.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageCapture)
//                    Timber.d("Binding Preview and ImageCapture only while waiting for helper.")
//                } catch (e: Exception) { Timber.e(e, "Error binding Preview/Capture only") }
            } else { // isPlaying이 false일 때
                Timber.d("isPlaying is false. Unbinding all.")
                cameraProvider?.unbindAll() // 모든 바인딩 해제
            }
        } else {
            Timber.d("CameraProvider, Preview or ImageCapture not ready yet.")
        }
    }

    // 사진 캡처 (변경 없음)
    LaunchedEffect(shouldCapture) {
        if (shouldCapture) {
            captureImage()
        }
    }

    // UI 부분
    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // *** 에러 메시지 표시 (예시) ***
        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }

        // *** 포즈 결과 시각화 (예시) ***
        poseResult?.let { bundle ->
            // bundle.results.firstOrNull()?.landmarks() 사용하여 Canvas 등에 그리기
            // 예: DrawLandmarksOverlay(result = bundle.results.first())
        }

        // 로딩 표시 (예시)
        if (isPlaying && !isHelperReady && errorMessage == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
// CameraViewModel에 imageAnalyzerExecutor 추가 필요
// @HiltViewModel class CameraViewModel ... {
//     val imageAnalyzerExecutor: ExecutorService = cameraExecutor // 또는 별도 생성
// }