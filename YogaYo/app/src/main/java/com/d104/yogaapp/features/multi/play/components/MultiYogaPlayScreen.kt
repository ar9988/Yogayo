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
import androidx.compose.ui.unit.dp
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
    isCountingDown: Boolean = false,
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
        userList.values.toList().sortedByDescending { it.roundScore }
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

                                    // 각 순위 행
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth() // 행 전체 너비 사용
                                            .padding(vertical = 4.dp), // 행 상하 패딩 살짝 추가
                                        verticalAlignment = Alignment.CenterVertically,
                                        // horizontalArrangement = Arrangement.SpaceBetween // 요소 간 간격 균등 분배 (이름 길면 깨질 수 있음)
                                    ) {
                                        // 등수 텍스트 (고정 너비 부여)
                                        Text(
                                            text = "${rank}등",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .width(50.dp) // 등수 표시 공간 확보
                                        )

                                        // 원형 테두리가 있는 사용자 아이콘 (변경 없음)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .border(1.dp, Color.Gray, CircleShape)
                                                .clip(CircleShape)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = "Person Icon"
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp)) // 아이콘과 이름 사이 간격

                                        // 닉네임 텍스트 (남은 공간 차지하도록 weight)
                                        Text(
                                            text = user.nickName, // 정렬된 user 객체에서 닉네임 가져오기
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .weight(1f) // 이름이 길 경우 다른 요소 밀어내도록
                                                .padding(horizontal = 8.dp)
                                        )

                                        // 점수(초) 텍스트 (고정 너비)
                                        Text(
                                            text = String.format("%.1f초", user.roundScore), // 소수점 첫째자리까지 표시
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier
                                                .width(70.dp) // 점수 표시 공간 확보
                                                .padding(horizontal = 8.dp)
                                        )
//
//                                        // 포인트 텍스트 (고정 너비)
//                                        Text(
//                                            text = "$points pt", // 계산된 포인트 사용
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            textAlign = TextAlign.End,
//                                            modifier = Modifier
//                                                .width(60.dp) // 포인트 표시 공간 확보
//                                                .padding(horizontal = 8.dp)
//                                        )
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
                            isCountingDown = isCountingDown,
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
                        if (isPlaying) {
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