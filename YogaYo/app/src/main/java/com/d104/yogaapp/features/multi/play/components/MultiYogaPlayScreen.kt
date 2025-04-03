package com.d104.yogaapp.features.multi.play.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d104.domain.model.PeerUser
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.CameraPreview
import com.d104.yogaapp.features.multi.play.GameState


@Composable
fun MultiYogaPlayScreen(
    timerProgress: Float,
    isPlaying: Boolean,
    onPause: () -> Unit,
    leftContent: @Composable () -> Unit,
    onImageCaptured: (Bitmap) -> Unit = {},
    gameState: GameState,
    isMenuClicked: Boolean,
    userList: MutableMap<String, PeerUser>
) {
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
                                items(userList.keys.toList()) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${it + 1}등",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                        )

                                        // 원형 테두리가 있는 사용자 아이콘
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

                                        Text(
                                            text = "${userList[it]?.nickName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                        )

                                        Text(
                                            text = "${userList[it]?.roundScore}초",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                        )

                                        Text(
                                            text = "${10} pt",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                        )


                                    }
                                }
                            }
                            Text(
                                modifier = Modifier.align(Alignment.TopEnd),
                                text = "5",
                                style = MaterialTheme.typography.bodyLarge
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
                            onImageCaptured = onImageCaptured,
                            poseId = 1.toString(),
                            shouldCapture = timerProgress == 0.5f
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
                                        .offset(x = (-6).dp, y = -4.dp), // 약간 왼쪽으로 이동
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