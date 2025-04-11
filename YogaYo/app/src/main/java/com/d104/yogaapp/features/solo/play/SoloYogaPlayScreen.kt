package com.d104.yogaapp.features.solo.play

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.CourseResultScreen
import com.d104.yogaapp.features.common.GifImage
import com.d104.yogaapp.utils.PermissionChecker
import com.d104.yogaapp.features.common.RotateScreen
import com.d104.yogaapp.features.common.YogaAnimationScreen
import com.d104.yogaapp.features.common.YogaPlayScreen
import com.d104.yogaapp.ui.theme.Neutral40
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun SoloYogaPlayScreen(
    course:UserCourse,
    viewModel: SoloYogaPlayViewModel = hiltViewModel(),
    isLogin:Boolean = false,
    onBackPressed: () -> Unit
) {

    LaunchedEffect(key1 = course.courseId) {
        viewModel.processIntent(SoloYogaPlayIntent.SetLoginState(isLogin))
        viewModel.processIntent(SoloYogaPlayIntent.InitializeWithCourse(course))
    }

    val state by viewModel.state.collectAsState()
    val currentPose by viewModel.currentPose.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

//    // 화면 설정 (가로 모드, 전체 화면)

    if (!state.isResult) {
        // 요가 플레이 중이거나 가이드 중일 때만 가로 모드로 설정
        RotateScreen(context)
    }
    if(state.isPlaying&&state.userCourse.tutorial&&!state.isCountingDown&&state.remainingTime>=10){
        viewModel.processIntent(SoloYogaPlayIntent.GoToNextPose)
    }

    LaunchedEffect(state.downloadState) {
        when (state.downloadState) {
            is DownloadState.Success -> {
                Toast.makeText(context, "이미지가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.processIntent(SoloYogaPlayIntent.ResetDownloadState)
            }
            is DownloadState.Error -> {
                val errorMessage = (state.downloadState as DownloadState.Error).message
                Toast.makeText(context, "저장 실패: $errorMessage", Toast.LENGTH_SHORT).show()
                viewModel.processIntent(SoloYogaPlayIntent.ResetDownloadState)
            }
            else -> {}
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if(state.isPlaying&&!state.isCountingDown) {
                        viewModel.processIntent(SoloYogaPlayIntent.TogglePlayPause)
                    }
                }
                else->{

                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // 컴포저블이 사라질 때 Observer 해제
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
        if(state.isPlaying&&!state.isCountingDown){
            viewModel.processIntent(SoloYogaPlayIntent.TogglePlayPause)
        }
    }

    // 권한에 따른 UI 표시
    if (state.cameraPermissionGranted) {
        // 권한이 있는 경우 요가 플레이 화면 표시
        if(state.isResult){
            Box(modifier = Modifier.fillMaxSize()){
                CourseResultScreen(
                    histories = state.poseHistories.toList(),
                    onFinish = {
                        onBackPressed()
                    },
                    onDownload = { uri,poseName->
                        viewModel.processIntent(SoloYogaPlayIntent.DownloadImage(uri,poseName))

                    }
                )
            }

        } else if(state.isGuide){
            Box(modifier = Modifier.fillMaxSize()) {
                if(state.userCourse.tutorial&& isNetworkConnected(context)){
                    VideoPlayer(
                        currentPose.poseVideo,
                        onVideoCompleted = { viewModel.processIntent(SoloYogaPlayIntent.ExitGuide) },
                    )
                }
                else {
                    PoseGuideScreen(
                        pose = currentPose,
                        onStartPose = { viewModel.processIntent(SoloYogaPlayIntent.ExitGuide) },
                    )
                }
                IconButton(
                    onClick = {viewModel.processIntent(SoloYogaPlayIntent.ExitGuide)},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                        .background(Neutral40, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip),
                        contentDescription = "튜토리얼 스킵",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }


        }else{
            Box(modifier = Modifier.fillMaxSize()) {
                YogaPlayScreen(
                    pose = currentPose,
                    timerProgress = state.timerProgress,
                    isPlaying = state.isPlaying,
                    onPause = { viewModel.processIntent(SoloYogaPlayIntent.TogglePlayPause) },
                    leftContent = {YogaAnimationScreen(pose = currentPose, accuracy = state.currentAccuracy, isPlaying = state.isPlaying)},
                    onSendResult = {pose,accuracy, time, bitmap: Bitmap ->  viewModel.processIntent(SoloYogaPlayIntent.SendHistory(pose,accuracy, time, bitmap))},
                    isCountingDown = state.isCountingDown,
                    isTutorial = state.userCourse.tutorial,
                    onAccuracyUpdate = {accuracy,time->
                        viewModel.processIntent(SoloYogaPlayIntent.SetCurrentHistory(accuracy,time))
                    }
                )

                // 일시정지 오버레이 표시
                if (!state.isPlaying&&!state.isCountingDown) {
                    PauseOverlay(
                        onResume = { viewModel.processIntent(SoloYogaPlayIntent.StartCountdown) },
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
                if (state.isCountingDown) {
                    CountdownOverlay(
                        seconds = 5,
                        onCountdownFinished = {
                            viewModel.processIntent(SoloYogaPlayIntent.FinishCountdown)
                        }
                    )
                }
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

@Composable
fun PoseGuideScreen(
    pose: YogaPose,
    onStartPose: () -> Unit
) {
    // TTS 초기화 및 상태 관리
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // TTS 인스턴스 생성
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }
    var currentDescriptionIndex by remember { mutableStateOf(-1) } // -1로 시작하면 아직 설명 시작 전
    var isReadingPoseName by remember { mutableStateOf(false) } // 포즈 이름 읽는 중인지 상태
    var isTtsReady by remember { mutableStateOf(false) }

    // TTS 초기화
    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.value?.language = Locale.getDefault()
                isTtsReady = true

                // 포즈 이름 먼저 읽기 시작
                isReadingPoseName = true
                textToSpeech.value?.speak(
                    pose.poseName,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "name"
                )
            }
        }

        // TTS 발화 진행 리스너 설정
        textToSpeech.value?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                coroutineScope.launch(Dispatchers.Main) {
                    delay(300)

                    if (utteranceId == "name") {
                        // 포즈 이름 읽기 완료, 설명 시작
                        isReadingPoseName = false

                        if (pose.poseDescriptions.isNotEmpty()) {
                            currentDescriptionIndex = 0

                            textToSpeech.value?.speak(
                                pose.poseDescriptions[0],
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "desc_0"
                            )
                        } else {
                            // 설명이 없는 경우
                            onStartPose()
                        }
                    }
                    else if (utteranceId?.startsWith("desc_") == true) {
                        val completedIndex = utteranceId.substringAfter("desc_").toIntOrNull() ?: -1

                        // 다음 설명으로 이동
                        if (completedIndex < pose.poseDescriptions.size - 1) {
                            currentDescriptionIndex = completedIndex + 1

                            // 다음 설명 읽기
                            textToSpeech.value?.speak(
                                pose.poseDescriptions[currentDescriptionIndex],
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "desc_$currentDescriptionIndex"
                            )
                        } else {
                            // 모든 설명 읽기 완료, onStartPose 실행
                            onStartPose()
                        }
                    }
                }
            }

            override fun onError(utteranceId: String?) {}
        })
    }

    // 컴포넌트 해제 시 TTS 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech.value?.stop()
            textToSpeech.value?.shutdown()
        }
    }

    // UI 부분
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
            ){
                GifImage(
                    url = pose.poseAnimation,
                    poseId = pose.poseId
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = pose.poseName,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    // 포즈 이름 읽는 중이면 강조
                    color = if (isReadingPoseName && isTtsReady)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 설명 텍스트들을 1줄씩 표시하고 현재 읽고 있는 항목 강조
                pose.poseDescriptions.forEachIndexed { index, description ->
                    Text(
                        text = "${index+1}.${description}",
                        style = MaterialTheme.typography.bodyLarge,
                        // 현재 읽고 있는 설명 강조
                        color = if (index == currentDescriptionIndex && !isReadingPoseName && isTtsReady)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CountdownOverlay(
    seconds: Int = 5,
    onCountdownFinished: () -> Unit
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // 전체 카운트다운 진행 상태 (1.0에서 0.0으로)
    val progress = remember { Animatable(1f) }
    // 현재 보여지는 초 값
    var displayedSecond by remember { mutableIntStateOf(seconds) }
    // 현재 진행 중인 애니메이션 Job
    var animationJob by remember { mutableStateOf<Job?>(null) }
    // 남은 애니메이션 시간 (밀리초 단위)
    var remainingDurationMillis by remember { mutableLongStateOf(seconds * 1000L) }
    // 일시정지 상태 플래그
    var isPaused by remember { mutableStateOf(false) }

    // onCountdownFinished 콜백이 recomposition 시에도 최신 상태를 유지하도록 함
    val currentOnCountdownFinished by rememberUpdatedState(onCountdownFinished)

    // Lifecycle 이벤트를 감지하여 애니메이션 제어
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    isPaused = true
                    // 현재 애니메이션 중지
                    animationJob?.cancel()
                    // 남은 시간 계산 (현재 progress 값 기준)
                    // progress.value는 1.0(시작) ~ 0.0(끝) 이므로, 남은 비율임
                    remainingDurationMillis = (progress.value * (seconds * 1000L)).toLong()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isPaused) { // 일시정지 상태에서 재개될 때만 실행
                        isPaused = false
                        if (remainingDurationMillis > 0) {
                            // 중단된 지점부터 남은 시간만큼 애니메이션 재시작
                            animationJob = coroutineScope.launch {
                                progress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = remainingDurationMillis.toInt(),
                                        easing = LinearEasing
                                    )
                                )
                                // 애니메이션 정상 종료 시 콜백 호출
                                if (!isPaused) { // 재개 후 다시 바로 pause 되지 않았는지 확인
                                    currentOnCountdownFinished()
                                }
                            }
                        } else if (remainingDurationMillis <= 0 && progress.value > 0f) {
                            // 만약 pause 시점에 이미 시간이 다 되었어야 하는데 progress가 0이 아니었다면
                            // 즉시 완료 처리
                            coroutineScope.launch {
                                progress.snapTo(0f) // 즉시 0으로 설정
                                currentOnCountdownFinished()
                            }
                        }
                    }
                }
                // 앱 종료 등 다른 Lifecycle 이벤트 처리 필요 시 추가
                Lifecycle.Event.ON_DESTROY -> {
                    animationJob?.cancel() // 확실하게 취소
                }
                else -> { /* 다른 이벤트 무시 */ }
            }
        }

        // Observer 등록
        lifecycleOwner.lifecycle.addObserver(observer)

        // 컴포저블이 사라질 때 Observer 해제
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // 컴포저블 제거 시 애니메이션 확실히 취소
            animationJob?.cancel()
        }
    }

    // 초기 애니메이션 시작 (앱이 처음 시작되거나 seconds 값이 변경될 때)
    LaunchedEffect(seconds) {
        // 초기화 (seconds 값이 바뀌면 처음부터 다시 시작)
        progress.snapTo(1f)
        remainingDurationMillis = seconds * 1000L
        isPaused = false // 상태 초기화
        animationJob?.cancel() // 이전 애니메이션 취소 (seconds 변경 시)

        // ON_RESUME 에서 시작하므로 여기서 바로 시작할 필요 없음
        // 단, 앱이 시작될 때 Lifecycle 상태가 이미 RESUMED 일 수 있으므로
        // 초기 상태 확인 후 시작하는 로직이 ON_RESUME 핸들러에 포함되어 있어야 함.
        // 만약 초기 상태가 RESUMED라면 ON_RESUME 이벤트가 발생하지 않을 수 있으므로,
        // 초기 시작 로직을 여기에 추가하거나, 현재 Lifecycle 상태를 확인하여 시작할 수 있음.
        // 아래 코드는 현재 상태가 RESUMED일 경우를 대비한 안전장치
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            // isPaused가 false이고 remainingDuration이 양수일 때 시작
            if (!isPaused && remainingDurationMillis > 0) {
                animationJob = coroutineScope.launch {
                    progress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = remainingDurationMillis.toInt(),
                            easing = LinearEasing
                        )
                    )
                    // 애니메이션 정상 종료 시 콜백 호출
                    if (!isPaused) { // 종료 시점에 pause 상태가 아닌지 확인
                        currentOnCountdownFinished()
                    }
                }
            }
        }
    }


    // 숫자 표시를 업데이트하는 LaunchedEffect (기존 로직 유지)
    LaunchedEffect(progress.value) {
        // 현재 진행 상태를 기반으로 표시할 초 계산 (약간의 오차 감안 + 0초 포함)
        val currentSeconds = ((progress.value * seconds) - 0.001f).coerceAtLeast(0f).toInt()
        // 표시되는 숫자가 실제 남은 시간보다 크거나 같을 때만 업데이트 (숫자가 줄어드는 것만 반영)
        if (displayedSecond > currentSeconds) {
            displayedSecond = currentSeconds
        }
        // progress가 거의 0에 도달했을 때 displayedSecond도 0이 되도록 보정
        if (progress.value < 0.01f && displayedSecond > 0) {
            displayedSecond = 0
        } else if (progress.targetValue == 1f && progress.value == 1f) { // 초기화 시
            displayedSecond = seconds
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "준비하세요!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White.copy(alpha = 0.3f),
                    strokeWidth = 8.dp,
                    progress = { 1f } // 항상 100%
                )
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    strokeWidth = 8.dp,
                    progress = { progress.value } // 애니메이션 진행률
                )
                // 숫자 카운트다운 (0초까지 표시되도록)
                Text(
                    // 초기에 seconds 값, 진행 중에는 계산된 값+1, 마지막 0초 표시
                    text = if (progress.value == 1f && seconds > 0) seconds.toString()
                    else if (displayedSecond == 0 && progress.value < 0.1f) "1" // 마지막 1초 표시 보정
                    else (displayedSecond + 1).toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "자세를 잡아주세요",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    onVideoCompleted: () -> Unit
) {
    val context = LocalContext.current
    val windowInfo = rememberWindowInfo()

    // 화면 정보에 따라 제약 조건 계산
    val screenAspectRatio = remember(windowInfo) {
        windowInfo.screenWidthDp.toFloat() / windowInfo.screenHeightDp.toFloat()
    }

    // ExoPlayer 인스턴스 생성
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
        }
    }

    // 비디오 정보를 추적하기 위한 상태
    var videoWidth by remember { mutableStateOf(0) }
    var videoHeight by remember { mutableStateOf(0) }
    var videoAspectRatio by remember { mutableStateOf(1f) }

    // 컴포넌트가 dispose될 때 ExoPlayer 해제
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // 비디오가 끝났을 때 콜백 실행
                    onVideoCompleted()
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                videoWidth = videoSize.width
                videoHeight = videoSize.height
                if (videoHeight > 0) {
                    videoAspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // 박스로 감싸서 표시
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val boxWidth = constraints.maxWidth
        val boxHeight = constraints.maxHeight
        val boxAspectRatio = boxWidth.toFloat() / boxHeight.toFloat()

        // 비디오 크기 계산 (비디오 종횡비 유지)
        val (videoViewWidth, videoViewHeight) = remember(videoAspectRatio, boxWidth, boxHeight) {
            if (videoAspectRatio > boxAspectRatio) {
                // 비디오가 더 넓은 경우: 너비를 맞추고 높이 조정
                Pair(boxWidth.toFloat(), boxWidth.toFloat() / videoAspectRatio)
            } else {
                // 비디오가 더 높은 경우: 높이를 맞추고 너비 조정
                Pair(boxHeight.toFloat() * videoAspectRatio, boxHeight.toFloat())
            }
        }

        AndroidView(
            modifier = Modifier
                .width(with(LocalDensity.current) { videoViewWidth.toDp() })
                .height(with(LocalDensity.current) { videoViewHeight.toDp() }),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL


                    setBackgroundColor(Color.Black.toArgb())
                }
            }
        )
    }
}

// 화면 정보를 가져오는 유틸리티 함수
@Composable
fun rememberWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        WindowInfo(
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp
        )
    }
}

data class WindowInfo(
    val screenWidthDp: Int,
    val screenHeightDp: Int
)

fun isNetworkConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    // API 23 (M) 이상 버전에 대한 처리
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false // 활성 네트워크 확인
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false // 네트워크 기능 확인

        // Wi-Fi, 모바일 데이터, 이더넷 중 하나라도 연결되어 있으면 true 반환
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        /*
        // 실제 인터넷 사용 가능 여부 확인 (더 엄격한 검사)
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        */

    } else {
        // API 23 미만 버전에 대한 처리 (Deprecated)
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}

