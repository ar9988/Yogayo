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
import androidx.compose.foundation.lazy.grid.GridItemSpan
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

    val itemsPerRow = 3
    val gridSpacing = 8.dp // 그리드 아이템 간 간격

    // --- 전체 화면을 LazyVerticalGrid로 변경 ---
    LazyVerticalGrid(
        columns = GridCells.Fixed(itemsPerRow), // 3열 고정
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp), // 그리드 전체 패딩
        horizontalArrangement = Arrangement.spacedBy(gridSpacing), // 아이템 간 수평 간격
        verticalArrangement = Arrangement.spacedBy(gridSpacing * 2) // 아이템 간 수직 간격 (수평보다 약간 넓게)
    ) {
        // --- 1. 프로필 섹션 (헤더 - 전체 너비 차지) ---
        item(span = { GridItemSpan(maxLineSpan) }) { // maxLineSpan: 현재 줄의 최대 스팬 (여기선 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                // .padding(16.dp) // contentPadding으로 대체 또는 조정
            ) {
                // 로그아웃 버튼 (우측 상단)
                IconButton(
                    onClick = { viewModel.processIntent(MyPageIntent.Logout) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 0.dp) // 위치 미세 조정
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "로그아웃",
                        modifier = Modifier.size(32.dp),
                    )
                }

                // 프로필 (중앙)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 프로필 이미지
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
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
            // 프로필 섹션과 다음 섹션 사이 간격
            // Spacer(modifier = Modifier.height(16.dp)) // verticalArrangement로 대체 또는 유지
        }

        // --- 2. 통계 섹션 (헤더 - 전체 너비 차지) ---
        item(span = { GridItemSpan(maxLineSpan) }) {
            UserRecordCard(
                userRecord = uiState.userRecord,
                showDetailButton = true,
                onClickDetail = {onNavigateToDetailRecord(uiState.userRecord)}
            )
            // 통계 카드와 다음 섹션 사이 간격
            // Spacer(modifier = Modifier.height(8.dp)) // verticalArrangement로 대체 또는 유지
        }

        // --- 3. 뱃지 섹션 타이틀 (헤더 - 전체 너비 차지) ---
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center // 중앙 정렬
            ) {
                Text(
                    text = "뱃지",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    // modifier = Modifier.padding(horizontal = 16.dp), // contentPadding으로 처리됨
                    textAlign = TextAlign.Center
                )
            }
            // 타이틀과 뱃지 그리드 사이 간격
            // Spacer(modifier = Modifier.height(12.dp)) // verticalArrangement로 대체 또는 유지
        }

        // --- 4. 뱃지 그리드 아이템들 ---
        // LazyVerticalGrid의 items 사용 (NonScrollableGrid 제거)
        items(uiState.myBadgeList, key = { it.badgeId ?: it.hashCode() }) { badge -> // 안정적인 고유 ID 사용 권장
            BadgeItem(badge = badge) // BadgeItem 직접 사용
        }

        // --- 5. 하단 추가 여백 (선택 사항) ---
        // 그리드 마지막 아이템 아래에 공간이 필요하면 추가
        // item(span = { GridItemSpan(maxLineSpan) }) {
        //     Spacer(modifier = Modifier.height(16.dp))
        // }
    }
}

// NonScrollableGrid 컴포저블은 이제 필요 없으므로 삭제합니다.
// @Composable fun NonScrollableGrid(...) { ... }


// BadgeItem 컴포저블은 이전과 동일하게 유지합니다.
// (단, 내부 Modifier.weight(1f) 같은 그리드 배치 관련 코드는 없어야 함)
@Composable
fun BadgeItem(badge: Badge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
        // .padding(4.dp) // Grid의 spacing으로 간격 처리, 필요시 내부 패딩 추가
        // .height(140.dp) // 높이 고정보다는 내용에 맞게 조절되도록 하는 것이 Grid에 더 적합할 수 있음
        // 고정 높이가 필요하다면 유지
    ) {
        // 뱃지 이미지
        Box(
            modifier = Modifier.size(80.dp), // 크기 유지 또는 조정
            contentAlignment = Alignment.Center
        ) {
            Image(
                 painter = painterResource(id = R.drawable.ic_badge_lv0),
                // 임시로 badge 종류에 따라 다른 아이콘 표시 예시
//                painter = painterResource(id = getBadgeResource(badge.badgeId)),
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

        // 다음 레벨 정보 및 진행 상태 표시 (이전과 동일)
        val nextLevelIndex = badge.highLevel
        val nextLevel = if (nextLevelIndex < badge.badgeDetails.size) {
            badge.badgeDetails[nextLevelIndex]
        } else null

        nextLevel?.let { level ->
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress = (badge.badgeProgress.toFloat() / level.badgeGoal.toFloat())
                    .coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progress }, // Compose 1.6+ 권장 람다 사용
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp)),
                    color = PrimaryColor,
                    trackColor = Color.LightGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${badge.badgeProgress}/${level.badgeGoal}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } ?: run {
            Text(
                text = "최고 레벨",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

//// 임시 함수: 뱃지 ID에 따라 다른 리소스 ID 반환 (실제 로직으로 대체 필요)
//fun getBadgeResource(badgeId: Long?): Int {
//    return when (badgeId) {
//        1L -> R.drawable.ic_badge_lv1 // 예시 리소스 ID
//        2L -> R.drawable.ic_badge_lv2
//        // ... 다른 뱃지 ID에 대한 매핑 ...
//        else -> R.drawable.ic_badge_lv0 // 기본 또는 알 수 없는 뱃지
//    }
//}


