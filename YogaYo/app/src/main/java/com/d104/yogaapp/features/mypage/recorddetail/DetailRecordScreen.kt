package com.d104.yogaapp.features.mypage.recorddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.MyPageInfo
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.UserRecordCard
import com.d104.yogaapp.ui.theme.PrimaryColor
import com.d104.yogaapp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRecordScreen(
    myPageInfo: MyPageInfo?, // Nullable 처리 고려
    viewModel: DetailRecordViewModel = hiltViewModel(),
    onBackPressed: () -> Unit, // 뒤로가기 추가 (필요 시)
    onNavigateToPoseHistory: (poseId: Long) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.handleIntent(DetailRecordIntent.initialize)
    }
    val state by viewModel.state.collectAsState()

    // userRecord가 null이면 로딩 또는 오류 표시
    if (myPageInfo == null && state.isLoading) { // 로딩 중이면서 userRecord 없을 때
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (myPageInfo == null) { // 로딩 끝났는데도 userRecord 없을 때 (오류 등)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("사용자 기록 정보를 불러올 수 없습니다.")
        }
        return
    }


    val itemsPerRow = 2
    val gridSpacing = 8.dp // 그리드 아이템 간 간격 통일

    // 전체 화면을 LazyVerticalGrid로 구성
    LazyVerticalGrid(
        columns = GridCells.Fixed(itemsPerRow),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(vertical = 8.dp), // 상하 패딩만 적용 (필요시 조절)
        horizontalArrangement = Arrangement.spacedBy(gridSpacing), // 열 간 간격
        verticalArrangement = Arrangement.spacedBy(gridSpacing * 2) // 행 간 간격
    ) {
        item(span = {GridItemSpan(maxLineSpan)}){

            TopAppBar(
                title = { Text("내 기록 상세보기") }, // state.poseDetail 사용 가능
                windowInsets = WindowInsets(top = 0),
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White, // 배경색과 동일하게
                    scrolledContainerColor = White
                )
            )

        }
        // 1. 상단 사용자 기록 카드 (전체 너비 차지하도록 span 설정)
        item(span = { GridItemSpan(maxLineSpan) }) { // maxLineSpan은 현재 줄의 최대 스팬(여기선 2)
            Column(modifier = Modifier.padding(horizontal = 12.dp)) { // 카드와 아래 컨텐츠 간 간격 확보용 Column
                // Spacer(modifier = Modifier.height(8.dp)) // contentPadding.vertical로 대체 가능
                UserRecordCard(
                    myPageInfo = myPageInfo, // 이제 non-null 보장
                    showDetailButton = false
                    // modifier = Modifier.padding(bottom = 16.dp) // 카드와 그리드 아이템 간 간격 -> 아래 Spacer로 조절
                )
                // UserRecordCard와 아래 그리드 아이템 사이의 간격
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 2. 포즈 기록 카드들
        items(state.bestPoseRecords, key = { it.poseId }) { pose -> // key 추가 권장
            BestPoseRecordCard(
                bestPoseHistory = pose,
                modifier = Modifier.clickable { // weight 제거, clickable만 적용
                    onNavigateToPoseHistory(pose.poseId)
                }
            )
        }

        // 3. 하단 추가 여백 (필요 시)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(16.dp)) // LazyColumn 마지막 Spacer와 유사 역할
        }
    }
}


@Composable
fun BestPoseRecordCard(
    bestPoseHistory: BestPoseRecord,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        color = White,
        contentColor = LocalContentColor.current,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 요가 포즈 이미지 (변경 없음)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(bestPoseHistory.poseImg)
                    .crossfade(true)
                    .error(R.drawable.ic_yoga)
                    .build(),
                contentDescription = bestPoseHistory.poseName,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 하단 텍스트 영역 (포즈 이름 + 지표들)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 포즈 이름 (변경 없음)
                Text(
                    text = bestPoseHistory.poseName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp)) // 이름과 지표 사이 간격 조정

                // 정확도와 시간을 함께 표시하는 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // SpaceEvenly: 항목들이 동일한 간격으로 배치됨
                    // SpaceAround: 항목 양 옆에 공간이 균등하게 배치됨
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 정확도 표시 (아이콘 + 값)
                    MetricItemShort( // 작은 지표 표시용 Composable (아래 정의)
                        icon = Icons.Filled.CheckCircleOutline,
                        value = String.format("%.1f%%", bestPoseHistory.bestAccuracy*100),
                        contentDescription = "정확도"
                    )

                    // 최고 시간 표시 (아이콘 + 값)
                    MetricItemShort(
                        icon = Icons.Outlined.Timer,
                        // formatDuration 함수를 사용하여 시간 포맷팅 (아래 정의 또는 import 필요)
                        value = String.format("%.2f초", bestPoseHistory.bestTime), // "mm:ss" 또는 다른 형식 사용 가능
                        contentDescription = "최고 시간"
                    )
                }
            }
        }
    }
}

// 작은 지표 (아이콘 + 값) 표시를 위한 재사용 컴포저블
@Composable
private fun MetricItemShort( // 이 컴포저블 내부에서만 사용될 가능성이 높으므로 private
    icon: ImageVector,
    value: String,
    contentDescription: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp) // 아이콘과 값 사이 간격
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(13.dp), // 아이콘 크기
            tint = PrimaryColor
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium, // 값 폰트 두께
            color = PrimaryColor // 값 색상
        )
    }
}

