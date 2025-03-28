package com.d104.yogaapp.features.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.Badge
import com.d104.domain.model.BadgeDetail
import com.d104.domain.model.UserRecord
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.UserRecordCard
import com.d104.yogaapp.ui.theme.GrayCardColor
import com.d104.yogaapp.ui.theme.PrimaryColor
import com.google.gson.JsonPrimitive
import kotlin.math.ceil

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onNavigateSoloScreen: () -> Unit,
    onNavigateToDetailRecord:(userRecord:UserRecord)->Unit
){
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.isLogoutSuccessful){
        if(uiState.isLogoutSuccessful){
            onNavigateSoloScreen()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(4.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { viewModel.processIntent(MyPageIntent.Logout) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)  // IconButton 자체의 크기 증가
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "로그아웃",
                        modifier = Modifier.size(32.dp),
                    )
                }

                // 프로필 섹션 (중앙)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), // 상단에 약간의 패딩 추가
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 프로필 이미지
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape) // 원형 클립 추가
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(if (uiState.userRecord.userProfile.isNotEmpty())
                                    uiState.userRecord.userProfile else R.drawable.ic_profile)
                                .crossfade(true)
                                .error(R.drawable.ic_profile)
                                .build(),
                            contentDescription = "프로필 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 사용자 이름
                    Text(
                        text = uiState.userRecord.userNickName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }



        // Stats Section
        item {
            UserRecordCard(
                userRecord = uiState.userRecord,
                showDetailButton = true,
                onClickDetail = {onNavigateToDetailRecord(uiState.userRecord)}
            )
            Spacer(modifier = Modifier.height(8.dp))

        }

        // Badge Section
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "뱃지",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign =TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Badge Grid
        item {
            // 그리드 컨텐츠를 계산하여 고정된 높이로 생성
            val badgeSize = 100.dp
            val textHeight = 16.dp
            val progressHeight = 20.dp // 진행 상태 표시 + 텍스트에 할당된 높이
            val itemSpacing = 16.dp
            val itemTotalHeight = badgeSize + textHeight + progressHeight + itemSpacing
            val numColumns = 3
            val numRows = ceil(uiState.myBadgeList.size.toFloat() / numColumns).toInt()
            val gridHeight = itemTotalHeight * numRows

            // 스크롤이 되지 않는 고정 크기의 그리드
            NonScrollableGrid(
                badges = uiState.myBadgeList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
                    .padding(horizontal = 16.dp)
            )

            // 하단 여백 추가
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}



@Composable
fun NonScrollableGrid(
    badges: List<Badge>,
    modifier: Modifier = Modifier
) {
    // LazyVerticalGrid 대신 일반 Column과 Row로 구현하여 스크롤 없이 표시
    Column(modifier = modifier) {
        val itemsPerRow = 3

        for (i in badges.indices step itemsPerRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (j in 0 until itemsPerRow) {
                    val index = i + j
                    if (index < badges.size) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgeItem(badge = badges[index])
                        }
                    } else {
                        // 빈 공간을 차지하는 Spacer
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 각 행 사이 간격 추가
            if (i + itemsPerRow < badges.size) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
fun BadgeItem(badge: Badge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .height(140.dp)
    ) {
        // 뱃지 이미지
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_badge_lv0),
                contentDescription = badge.badgeName,
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 뱃지 이름
        Text(
            text = badge.badgeName,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 다음 레벨 정보 및 진행 상태 표시
        val nextLevelIndex = badge.highLevel
        val nextLevel = if (nextLevelIndex < badge.badgeDetails.size) {
            badge.badgeDetails[nextLevelIndex]
        } else null

        nextLevel?.let { level ->
            // 현재 진행 상태 표시
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                // 진행 상태 바
                val progress = (badge.badgeProgress.toFloat() / level.badgeGoal.toFloat())
                    .coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp)),
                    color = PrimaryColor, // 프로그레스 바 색상
                    trackColor = Color.LightGray // 배경 색상
                )
                Spacer(modifier = Modifier.height(2.dp))
                // 진행 상태 텍스트 (예: "6/10")
                Text(
                    text = "${badge.badgeProgress}/${level.badgeGoal}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )


            }
        } ?: run {
            // 만약 다음 레벨이 없다면 (최고 레벨에 도달)
            Text(
                text = "최고 레벨",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}


