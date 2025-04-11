package com.d104.yogaapp.features.multi.play.components

import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.PeerUser
import com.d104.domain.model.YogaPose
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.CameraPreview
import com.d104.yogaapp.features.multi.play.GameState
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.Locale


@Composable
fun MultiYogaPlayScreen(
    timerProgress: Float,
    isPlaying: Boolean,
    onPause: () -> Unit,
    leftContent: @Composable () -> Unit,
    onSendResult: (YogaPose, Float, Float, Bitmap) -> Unit ={ _, _, _, _->},
    gameState: GameState,
    isMenuClicked: Boolean,
    userList: Map<String, PeerUser>,
    pose: YogaPose,
    onAccuracyUpdate:(Float,Float)->Unit = {_,_->}
) {
    // TTS 인스턴스 생성
    val context = LocalContext.current
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    val sortedUserList = remember(userList) {
        Timber.d("Recalculating sorted user list. Current size: ${userList.size}")
        userList.values.toList()
            .sortedWith(compareByDescending<PeerUser> { it.roundScore }
                .thenBy { it.id })
    }

    var roundResultRemainingTime by remember { mutableStateOf(10) }
    LaunchedEffect(key1 = gameState) {
        if (gameState == GameState.RoundResult) {
            Timber.d("RoundResult state detected. Starting 10s countdown.")
            roundResultRemainingTime = 10 // 상태 진입 시 10으로 초기화
            for (i in 10 downTo 0) {
                roundResultRemainingTime = i
                delay(1000L) // 1초 대기
            }
            // 타이머 종료 후 추가 작업이 필요하면 여기에 작성 (예: 다음 상태로 자동 전환 트리거)
            Timber.d("RoundResult countdown finished.")
        }
    }
    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.value?.language = Locale.getDefault()
                isTtsReady = true
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech.value?.stop()
            textToSpeech.value?.shutdown()
        }
    }
    when (gameState) {
        GameState.RoundResult -> {
            Box(
                modifier = Modifier.fillMaxSize()
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
                            .fillMaxHeight()
                            .padding(end = 5.dp, bottom = 16.dp)
                    ) {
                        leftContent()
                    }
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                    ) {
                        // 결과 화면
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 32.dp)
                            ) {
                                itemsIndexed(sortedUserList) { index, user ->
                                    val rank = index + 1 // 0부터 시작하므로 +1
                                    val points = when (rank) {
                                        1 -> 10
                                        2 -> 7
                                        3 -> 4
                                        4 -> 1
                                        else -> 0 // 5등부터는 0점
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // 1. 등수 (고정 너비)
                                        Text(
                                            text = "${rank}등",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(45.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // 2. 아이콘 (고정 크기)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp) // 아이콘 크기
                                                .border(1.dp, Color.Gray, CircleShape)
                                                .clip(CircleShape)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(user.iconUrl.ifEmpty { R.drawable.ic_profile })
                                                    .crossfade(true)
                                                    .error(R.drawable.ic_profile)
                                                    .build(),
                                                contentDescription = "프로필 이미지",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // 3. 닉네임 (고정 너비 + Ellipsis)
                                        Text(
                                            text = user.nickName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1, // 한 줄로 제한
                                            overflow = TextOverflow.Ellipsis, // 넘치면 ... 처리
                                            modifier = Modifier
                                                // --- 중요: 여기에 닉네임을 위한 고정 너비 지정 ---
                                                // 예시 값, 실제 앱에 맞게 조절하세요.
                                                .width(60.dp)
                                        )

                                        // 4. 고정 간격 Spacer (weight(1f) 대신 사용)
                                        // 닉네임과 점수 사이의 원하는 간격을 지정합니다.
                                        Spacer(modifier = Modifier.width(12.dp)) // 예시 간격

                                        // 5. 점수 (고정 너비, 오른쪽 정렬)
                                        Text(
                                            text = String.format("%.1f초", user.roundScore),
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.width(75.dp)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // 6. 포인트 (고정 너비, 오른쪽 정렬)
                                        Text(
                                            text = "$points pt",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.width(65.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp), // 패딩 조정
                                // 상태 변수 사용
                                text = "$roundResultRemainingTime",
                                style = MaterialTheme.typography.titleLarge // 크기 조절 (원하는 대로)
                            )
                        }
                    }
                }
            }
        }

        else -> {
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
                            .padding(end = 5.dp, bottom = 16.dp)
                    ) {
                        leftContent()
                    }
                    Spacer(
                        modifier = Modifier
                            .weight(0.02f)
                    )
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
                            onSendResult = onSendResult,
                            pose = pose,
                            isCountingDown = gameState == GameState.Waiting,
                            onRessultFeedback = {accuracy,time,feedback->
                                onAccuracyUpdate(accuracy,time )
                                if (feedback.isNotEmpty() && isTtsReady&&isPlaying) {
                                    textToSpeech.value?.let{textToSpeech->
                                        if(!textToSpeech.isSpeaking){
                                            textToSpeech.speak(
                                                feedback,
                                                TextToSpeech.QUEUE_ADD, // QUEUE_FLUSH 대신 QUEUE_ADD 사용
                                                null,
                                                "text_${System.currentTimeMillis()}" // 고유한 식별자 사용
                                            )
                                        }

                                    }
                                }
                            }
                        )
                        if (isPlaying&&gameState == GameState.Playing) {
                            // 현재 등수 이미지 추가하기

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
                                        .offset(x = (-6).dp, y = (-4).dp), // 약간 왼쪽으로 이동
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }


                        // 일시정지/재생 버튼
                        if (!isMenuClicked) {
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
                    }
                }
            }
        }
    }
}