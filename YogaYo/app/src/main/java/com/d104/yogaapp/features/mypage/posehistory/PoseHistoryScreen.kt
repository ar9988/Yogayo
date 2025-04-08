package com.d104.yogaapp.features.mypage.posehistory

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.YogaHistory
import com.d104.yogaapp.R // 앱의 리소스 경로 확인
import com.d104.yogaapp.features.common.YogaAccuracyTimeChart
import com.d104.yogaapp.features.common.YogaPoseDetailDialog
import com.d104.yogaapp.features.solo.play.DownloadState
import com.d104.yogaapp.ui.theme.GrayCardColor
import com.d104.yogaapp.ui.theme.PastelBlue
import com.d104.yogaapp.ui.theme.PastelRed
import com.d104.yogaapp.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoseHistoryScreen(
    viewModel: PoseHistoryViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var selectedHistoryForDialog by remember { mutableStateOf<YogaHistory?>(null) }
    val context = LocalContext.current

    var selectedSortOption by remember { mutableStateOf(CombinedSortOption.DATE_DESC) } // 기본: 최신순
    var dropdownExpanded by remember { mutableStateOf(false) } // 드롭다운 메뉴 확장 상태
    val sortedHistories = remember(state.poseDetail.histories, selectedSortOption) {
        sortHistories(state.poseDetail.histories, selectedSortOption)
    }


    LaunchedEffect(state.downloadState) {
        when (state.downloadState) {
            is DownloadState.Success -> {
                Toast.makeText(context, "이미지가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.processIntent(PoseHistoryIntent.ResetDownloadState)
            }
            is DownloadState.Error -> {
                val errorMessage = (state.downloadState as DownloadState.Error).message
                Toast.makeText(context, "저장 실패: $errorMessage", Toast.LENGTH_SHORT).show()
                viewModel.processIntent(PoseHistoryIntent.ResetDownloadState)
            }
            else -> {}
        }
    }

    // --- 다이얼로그 표시 로직 ---
    selectedHistoryForDialog?.let { historyToShow ->
        YogaPoseDetailDialog(
            history = historyToShow,
            onDismiss = { selectedHistoryForDialog = null }, // 다이얼로그 닫기 요청 시 상태를 null로 변경
            onDownload = { uri, poseName ->
                viewModel.downloadImage(uri,poseName)
            }
        )
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                // 1. 로딩 중 상태 처리
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // 2. 에러 상태 처리
                state.error != null -> {
                    Text(
                        text = "오류: ${state.error}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = Color.Red
                    )
                }
                // 3. 데이터 로딩 성공 상태 처리
                // state.poseDetail이 null이 아닐 경우에만 LazyColumn 표시
                else -> {
                    // state.poseDetail을 nullable 변수로 받아서 사용 (Smart Cast 활용)
                    val poseDetail = state.poseDetail
                    if (poseDetail != null) {
                        // poseDetail이 null이 아님이 보장되는 블록
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            // 아이템 간 수직 간격은 유지
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                TopAppBar(
                                    title = { Text(poseDetail.poseName) }, // state.poseDetail 사용 가능
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

                            // --- 이하 아이템들에는 수평 패딩(16.dp)을 개별적으로 추가 ---
                            val horizontalPaddingModifier = Modifier.padding(horizontal = 16.dp)

                            // 2. 포즈 이미지 아이템 - 패딩 추가
                            item {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(poseDetail.poseImg)
                                        .crossfade(true)
                                        .error(R.drawable.ic_yoga)
                                        .build(),
                                    contentDescription = poseDetail.poseName,
                                    modifier = Modifier // 기존 Modifier 체인 시작
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .then(horizontalPaddingModifier) // 수평 패딩 적용
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }


                            // 4. 주요 지표 카드 아이템 - 패딩 추가
                            item {
                                PoseMetricsCard(
                                    accuracy = poseDetail.bestAccuracy,
                                    maxTime = poseDetail.bestTime,
                                    count = poseDetail.histories.size,
                                    winCount = poseDetail.winCount,
                                    modifier = horizontalPaddingModifier // Card 자체 Modifier에 패딩 적용
                                )
                            }

                            // 5. 차트 영역 아이템 - 패딩 추가
                            if(state.poseDetail.histories.size>1) {
                                item {
                                    YogaAccuracyTimeChart(state.poseDetail.histories)
                                }
                            }

                            // 6. 기록 제목 아이템 - 패딩 추가
                            item {
                                Text(
                                    text = "자세 기록",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(top = 8.dp) // 기존 상단 패딩 유지
                                        .then(horizontalPaddingModifier) // 수평 패딩 적용
                                )
                            }




                            // 7. 요가 기록 리스트 아이템들 - 패딩 추가
                            if (poseDetail.histories.isEmpty()) {
                                item {
                                    Text(
                                        text = "아직 이 자세에 대한 기록이 없습니다.",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp)
                                            .then(horizontalPaddingModifier) // 수평 패딩 적용
                                    )
                                }
                            } else {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            // 버튼 영역에도 좌우 패딩(16.dp)을 동일하게 적용
                                            .padding(horizontal = 16.dp)
                                            // 필요하다면 버튼 상하 패딩 추가 (예: vertical = 8.dp)
                                            .padding(vertical = 4.dp) // 리스트 아이템과의 간격 조절
                                    ) {
                                        // 2. 안쪽 Box: 실제 버튼과 드롭다운 메뉴를 포함하며, 오른쪽 정렬됨
                                        Box(
                                            // *** Modifier.align(Alignment.CenterEnd) 추가 ***
                                            // 바깥 Box 내에서 오른쪽 중앙으로 정렬
                                            modifier = Modifier.align(Alignment.CenterEnd)
                                        ) {
                                            IconButton(onClick = { dropdownExpanded = true }) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // 현재 선택된 정렬 아이콘 표시 (Sort 아이콘 사용 예시)
                                                    Icon(
                                                        // imageVector = selectedSortOption.icon, // 또는 고정 아이콘 사용
                                                        imageVector = Icons.AutoMirrored.Filled.Sort, // 일반 정렬 아이콘
                                                        contentDescription = "현재 정렬: ${selectedSortOption.displayName}",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    // 드롭다운 화살표
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDropDown,
                                                        contentDescription = "정렬 옵션 변경"
                                                    )
                                                }
                                            }

                                            // 드롭다운 메뉴 (위치는 버튼 기준으로 자동 조절됨)
                                            DropdownMenu(
                                                expanded = dropdownExpanded,
                                                onDismissRequest = { dropdownExpanded = false }
                                            ) {
                                                CombinedSortOption.values().forEach { option ->
                                                    DropdownMenuItem(
                                                        // *** text 파라미터 수정 ***
                                                        text = {
                                                            // Row를 사용하여 텍스트와 아이콘을 가로로 배치
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically // 수직 중앙 정렬
                                                            ) {
                                                                // 1. 옵션 표시 이름 텍스트
                                                                Text(option.displayName)

                                                                // 2. 텍스트와 아이콘 사이 간격 (선택적)
                                                                Spacer(modifier = Modifier.width(4.dp)) // 작은 간격 추가

                                                                // 3. 정렬 방향 아이콘 (오름차순/내림차순)
                                                                Icon(
                                                                    imageVector = if (option.order == SortOrder.ASCENDING)
                                                                        Icons.Default.ArrowUpward // 오름차순 아이콘
                                                                    else
                                                                        Icons.Default.ArrowDownward, // 내림차순 아이콘
                                                                    contentDescription = if (option.order == SortOrder.ASCENDING) "오름차순" else "내림차순",
                                                                    // 아이콘 크기나 색상 조절 (선택적)
                                                                    modifier = Modifier.size(16.dp), // 아이콘 크기 약간 작게
                                                                    tint = LocalContentColor.current.copy(alpha = 0.6f) // 기본 텍스트 색상보다 약간 연하게
                                                                )
                                                            }
                                                        },
                                                        onClick = {
                                                            selectedSortOption = option // 상태 업데이트
                                                            dropdownExpanded = false // 메뉴 닫기
                                                        },
                                                        // *** leadingIcon 파라미터 제거 ***
                                                        // leadingIcon = { /* 기존 코드 제거 */ },

                                                        // 현재 선택된 항목 강조 (기존과 동일)
                                                        modifier = if (selectedSortOption == option) Modifier.background(
                                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                                        ) else Modifier
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                // --- 드롭다운 버튼 끝 ---


                                items(
                                    items = sortedHistories,
                                    key = { it.createdAt ?: it.hashCode() }
                                ) { record ->
                                    // YogaPoseRecordItem 자체는 패딩 없이 호출하고,
                                    // 여기서 Modifier로 감싸서 패딩을 줄 수도 있음.
                                    // 또는 YogaPoseRecordItem 내부 Card에 패딩이 없다면 아래처럼 적용.
                                    Box(modifier = horizontalPaddingModifier) { // Box로 감싸서 패딩 적용
                                        YogaPoseRecordItem(
                                            record = record,
                                            onItemClick = { selectedHistoryForDialog = it }
                                            // YogaPoseRecordItem의 modifier는 사용 안 함
                                        )
                                    }
                                }
                            }

                            // 8. LazyColumn 맨 아래 여백 아이템 - 패딩 불필요
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    } else {
                        // 로딩도 아니고 에러도 아닌데 poseDetail이 null인 경우 (예상치 못한 상태)
                        Text(
                            text = "포즈 상세 정보를 표시할 수 없습니다.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                        )
                    }
                }
            }
        }

}


@Composable
fun PoseMetricsCard(
    accuracy: Float,
    maxTime: Float,
    count: Int,
    winCount:Int,
    modifier:Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = GrayCardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // 그림자 없음
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp), // 내부 패딩 늘림
            verticalArrangement = Arrangement.spacedBy(12.dp) // 항목 간 간격 늘림
        ) {
            MetricItem(label = "베스트 정확도 :", value = String.format("%.1f%%", accuracy))
            MetricItem(label = "최대 유지 시간:", value = String.format("%.2f초", maxTime))
            MetricItem(label = "자세 수행 횟수:", value = "${count}회")
            MetricItem(label = "우승 횟수:", value = "${winCount}회")
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // 양 끝 정렬
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = Color.DarkGray) // 글자 크기 및 색상 조정
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium) // 글자 크기 및 굵기 조정
    }
}

// --- 차트 Placeholder ---
@Composable
fun ChartPlaceholder(
    chartData: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp), // 차트 영역 높이
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrayCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center) {
            if (chartData.isEmpty()) {
                Text("차트 데이터가 없습니다.")
            } else {
                // 여기에 실제 차트 라이브러리 연동
                // 예시 텍스트: 마지막 데이터 표시
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("차트 영역 (구현 필요)", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("표시할 데이터: ${chartData.size}개")
                    chartData.lastOrNull()?.let {
                        Text("최근: ${it.first} - ${String.format("%.1f", it.second)}%")
                    }
                }
            }
        }
    }
}


@Composable
fun YogaPoseRecordItem(
    record: YogaHistory,
    onItemClick: (YogaHistory) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable { onItemClick(record) },
        shape = RoundedCornerShape(12.dp), // 모서리 더 둥글게
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 약간의 그림자
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), // 패딩 조정
            verticalArrangement = Arrangement.spacedBy(8.dp) // 요소 간 기본 간격
        ) {
            // --- 상단 정보: 태그 + 날짜 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 솔로/멀티 태그 (Chip 스타일 예시)
                val (tagText, tagColor) = if (record.ranking != null) {
                    "멀티 ${record.ranking}위" to PastelRed
                } else {
                    "솔로" to PastelBlue
                }
                Box(
                    modifier = Modifier
                        .background(tagColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)) // 연한 배경
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tagText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = tagColor
                    )
                }

                // 날짜
                Text(
                    text = formatTimestamp(record.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // --- 구분선 (선택 사항) ---
            // Divider(color = Color.LightGray.copy(alpha = 0.5f))

            // --- 주요 지표: 정확도 + 수행 시간 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround, // 공간 분배
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 정확도
                MetricWithIcon(
                    icon = Icons.Default.CheckCircleOutline, // 예시 아이콘
                    label = "정확도",
                    value = String.format("%.1f%%", record.accuracy)
                )

                // 수행 시간
                MetricWithIcon(
                    icon = Icons.Outlined.Timer, // 예시 아이콘
                    label = "수행 시간",
                    String.format("%.2f초", record.poseTime)
                )
            }
        }
    }
}

// 재사용 가능한 지표 표시 컴포저블
@Composable
fun MetricWithIcon(icon: ImageVector, label: String, value: String) {
    Column( // 전체를 Column으로 감싸 세로 배치
        horizontalAlignment = Alignment.CenterHorizontally, // 내부 요소들 가로 중앙 정렬 (선택 사항)
        verticalArrangement = Arrangement.spacedBy(2.dp) // 아이콘+레이블과 값 사이 간격
    ) {
        // 상단: 아이콘 + 레이블
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp) // 아이콘과 레이블 사이 간격
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp), // 레이블과 같은 줄에 있으니 아이콘 크기 약간 줄여도 좋음
                tint = MaterialTheme.colorScheme.primary // 아이콘 색상 (또는 Color.Gray 등)
            )
            Text(
                text = label,
                fontSize = 14.sp, // 레이블 폰트 크기
                color = Color.Gray // 레이블 색상
            )
        }

        // 하단: 값
        Text(
            text = value,
            fontSize = 16.sp, // 값을 더 강조하기 위해 폰트 크기 키움
            fontWeight = FontWeight.SemiBold // 굵기 조정
            // modifier = Modifier.padding(top = 2.dp) // Row와 Text 사이 간격을 Spacer 대신 padding으로 줄 수도 있음
        )
    }
}




// 타임스탬프(Long)를 날짜/시간 문자열로 변환하는 헬퍼 함수
fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "시간 정보 없음"
    return try {
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "시간 형식 오류"
    }
}

// 초(Float)를 "mm:ss" 형식의 문자열로 변환하는 헬퍼 함수
fun formatDuration(seconds: Float): String {
    val totalSeconds = seconds.toLong()
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds)
    val remainingSeconds = totalSeconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

enum class SortCriteria(val displayName: String) {
    DATE("시간순"),
    ACCURACY("정확도순"),
    POSE_TIME("운동 시간순")
}

// 정렬 순서를 나타내는 enum
enum class SortOrder {
    ASCENDING, DESCENDING
}

enum class CombinedSortOption(
    val displayName: String,
    val criteria: SortCriteria,
    val order: SortOrder,
) {
    DATE_DESC("시간순", SortCriteria.DATE, SortOrder.DESCENDING),
    DATE_ASC("시간순", SortCriteria.DATE, SortOrder.ASCENDING),
    ACCURACY_DESC("정확도순", SortCriteria.ACCURACY, SortOrder.DESCENDING),
    ACCURACY_ASC("정확도순", SortCriteria.ACCURACY, SortOrder.ASCENDING),
    TIME_DESC("유지 시간순", SortCriteria.POSE_TIME, SortOrder.DESCENDING),
    TIME_ASC("유지 시간순", SortCriteria.POSE_TIME, SortOrder.ASCENDING)
}
private fun sortHistories(
    histories: List<YogaHistory>,
    sortOption: CombinedSortOption // 파라미터를 통합 옵션으로 변경
): List<YogaHistory> {
    val comparator = when (sortOption.criteria) { // 옵션에서 기준(criteria) 사용
        SortCriteria.DATE -> compareBy<YogaHistory, Long?>(nullsLast()) { it.createdAt }
        SortCriteria.ACCURACY -> compareBy { it.accuracy }
        SortCriteria.POSE_TIME -> compareBy { it.poseTime }
    }

    return if (sortOption.order == SortOrder.DESCENDING) { // 옵션에서 순서(order) 사용
        histories.sortedWith(comparator.reversed())
    } else {
        histories.sortedWith(comparator)
    }
}