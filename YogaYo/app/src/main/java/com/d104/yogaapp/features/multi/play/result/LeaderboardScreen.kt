package com.d104.yogaapp.features.multi.play.result

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person // 유지
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // 유지
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.domain.model.PeerUser // PeerUser 임포트
import com.d104.yogaapp.R
import kotlinx.coroutines.delay


@Composable
fun LeaderboardScreen(
    // scores 파라미터 제거, userList 파라미터 추가
    userList: Map<String, PeerUser>,
    onNextClick: () -> Unit
) {
    // State to control the visibility of list items for animation
    var visibleItems by remember { mutableIntStateOf(0) }

    // --- 데이터 처리: userList를 totalScore 기준 내림차순 정렬 ---
    val sortedUsers = remember(userList) { // userList가 변경될 때만 재계산
        userList.values.sortedByDescending { it.totalScore }
    }
    // ---------------------------------------------------------

    // Trigger the animation when the composable enters the composition
    // LaunchedEffect의 key를 sortedUsers 또는 userList로 변경
    LaunchedEffect(key1 = sortedUsers) {
        visibleItems = 0 // Reset if scores change
        sortedUsers.indices.forEach { index ->
            delay(300L) // Delay between each item appearing
            visibleItems = index + 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Winner Section ---
        // sortedUsers의 첫 번째 항목을 승자로 전달 (null 가능성 처리)
        WinnerSection(winner = sortedUsers.firstOrNull())

        Spacer(modifier = Modifier.height(40.dp))

        // --- Rankings List ---
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp) // Spacing between rank items
        ) {
            // 정렬된 사용자 리스트로 반복
            sortedUsers.forEachIndexed { index, player ->
                // Animate each item's appearance
                AnimatedVisibility(
                    visible = index < visibleItems,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(durationMillis = 500)
                    ) + fadeIn(animationSpec = tween(durationMillis = 500)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    // RankItem에 PeerUser 객체 전달
                    RankItem(rank = index + 1, player = player)
                }
            }
        }

        // Pushes the button to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // --- Next Button ---
        Button(
            onClick = { onNextClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF48FB1)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "다음",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun WinnerSection(winner: PeerUser?) { // PlayerScore 대신 PeerUser?, Nullable 처리
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.ic_crown),
            contentDescription = "Winner Crown",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        // 승자 정보가 있을 때만 표시
        winner?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Person Icon"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = it.nickName, // nickName 사용
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                // 필요하다면 승자 점수도 표시
                // Spacer(modifier = Modifier.width(8.dp))
                // Text(text = "(${it.totalScore} PT)", fontSize = 16.sp)
            }
        } ?: Spacer(modifier = Modifier.height(24.dp)) // 승자 없으면 빈 공간
    }
}

@Composable
fun RankItem(rank: Int, player: PeerUser) { // PlayerScore 대신 PeerUser 사용
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Indicator (Medal or Text)
        RankIndicator(rank = rank)

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            Icons.Filled.Person,
            contentDescription = "Person Icon"
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Player Name (takes up available space)
        Text(
            text = player.nickName, // nickName 사용
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )

        // Score
        Text(
            text = "${player.totalScore}PT", // totalScore 사용
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
    }
}


@Composable
fun RankIndicator(rank: Int) {
    Box(
        modifier = Modifier.width(40.dp), // Fixed width for alignment
        contentAlignment = Alignment.Center
    ) {
        when (rank) {
            1 -> Image(
                painter = painterResource(id = R.drawable.ic_gold_medal), // Replace
                contentDescription = "1st Place",
                modifier = Modifier.size(32.dp) // Adjust medal size
            )
            2 -> Image(
                painter = painterResource(id = R.drawable.ic_silver_medal), // Replace
                contentDescription = "2nd Place",
                modifier = Modifier.size(32.dp) // Adjust medal size
            )
            3 -> Image(
                painter = painterResource(id = R.drawable.ic_bronze_medal), // Replace
                contentDescription = "3rd Place",
                modifier = Modifier.size(32.dp) // Adjust medal size
            )
            else -> Text(
                text = "${rank}th",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Gray // Color for ranks > 3
            )
        }
    }
}
