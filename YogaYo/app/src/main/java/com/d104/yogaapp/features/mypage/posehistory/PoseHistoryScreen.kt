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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.ChartData
import com.d104.domain.model.YogaHistory
import com.d104.yogaapp.R // 앱의 리소스 경로 확인
import com.d104.yogaapp.features.common.YogaPoseDetailDialog
import com.d104.yogaapp.features.solo.play.DownloadState
import com.d104.yogaapp.ui.theme.GrayCardColor
import com.d104.yogaapp.ui.theme.PastelBlue
import com.d104.yogaapp.ui.theme.PastelRed
import com.d104.yogaapp.ui.theme.PrimaryColor
import com.d104.yogaapp.ui.theme.White
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.marker.Marker
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoseHistoryScreen(
    viewModel: PoseHistoryViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var selectedHistoryForDialog by remember { mutableStateOf<YogaHistory?>(null) }
    val context = LocalContext.current

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

                            // 3. 포즈 이름 아이템 - 패딩 추가
//                            item {
//                                Text(
//                                    text = poseDetail.poseName,
//                                    fontSize = 24.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .then(horizontalPaddingModifier), // 수평 패딩 적용
//                                    textAlign = TextAlign.Center
//                                )
//                            }

                            // 4. 주요 지표 카드 아이템 - 패딩 추가
                            item {
                                PoseMetricsCard(
                                    accuracy = poseDetail.bestAccuracy,
                                    maxTime = poseDetail.bestTime,
                                    count = poseDetail.winCount,
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
                                items(
                                    items = poseDetail.histories,
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
            MetricItem(label = "베스트 정확도 :", value = String.format("%.1f%%", accuracy*100))
            MetricItem(label = "최대 유지 시간:", value = String.format("%.2f초", maxTime))
            MetricItem(label = "자세 수행 횟수:", value = "${count}회")
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
                    value = String.format("%.1f%%", record.accuracy * 100)
                )

                // 수행 시간
                MetricWithIcon(
                    icon = Icons.Outlined.Timer, // 예시 아이콘
                    label = "수행 시간",
                    value = formatDuration(record.poseTime)
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

@Composable
fun rememberYogaHistoryChartProducer(
    yogaHistoryList: List<YogaHistory>
): Pair<ChartEntryModelProducer, List<YogaHistory>> {

    val modelProducer = remember { ChartEntryModelProducer() }
    // 1. 원본 데이터 정렬 (X축 포매터용)
    val sortedHistory = remember(yogaHistoryList) {
        yogaHistoryList
            .filter { it.createdAt != null }
            .sortedBy { it.createdAt }
    }

    // 2. 시리즈 1: 실제 데이터 ChartEntry 리스트
    val realChartEntries: List<ChartEntry> = remember(sortedHistory) {
        sortedHistory.mapIndexed { index, history ->
            entryOf(index.toFloat(), history.accuracy*100)
        }
    }

    // 3. 시리즈 2 & 3: 투명 경계선용 데이터 (0% 및 100%)
    val boundaryEntries0: List<ChartEntry> = remember(realChartEntries) {
        if (realChartEntries.isNotEmpty()) {
            val minX = realChartEntries.first().x
            val maxX = realChartEntries.last().x
            // 시작과 끝 X 지점에 Y=0 값 추가
            listOf(entryOf(minX, 0f), entryOf(maxX, 0f))
        } else {
            emptyList()
        }
    }
    val boundaryEntries100: List<ChartEntry> = remember(realChartEntries) {
        if (realChartEntries.isNotEmpty()) {
            val minX = realChartEntries.first().x
            val maxX = realChartEntries.last().x
            // 시작과 끝 X 지점에 Y=100 값 추가
            listOf(entryOf(minX, 100f), entryOf(maxX, 100f))
        } else {
            emptyList()
        }
    }

    // 4. 3개의 시리즈를 포함하는 ChartEntryModel 생성 및 Producer 업데이트
    LaunchedEffect(realChartEntries, boundaryEntries0, boundaryEntries100) {
        if (realChartEntries.isNotEmpty()) {
            // 3개의 시리즈를 entryModelOf 또는 entryModel()을 사용하여 모델로 만듦
            val multiSeriesData = listOf(realChartEntries, boundaryEntries0, boundaryEntries100)
            println("Updating chart producer with List<List<ChartEntry>>.")
            // setEntries 메소드에 이 리스트 자체를 전달
            modelProducer.setEntries(multiSeriesData)
            // 애니메이션 원하면: modelProducer.setEntriesSuspending(chartModel)
        } else {
            modelProducer.setEntries(emptyList<ChartEntry>()) // 데이터 없으면 비우기
        }
    }

    // Producer와 정렬된 '원본' 데이터 리스트 반환 (X축 포매터용)
    return remember(modelProducer, sortedHistory) { modelProducer to sortedHistory }
}


class FixedStepVerticalAxisPlacer : AxisItemPlacer.Vertical {

    // 표시할 고정된 Y축 값 리스트
    private val fixedValues = listOf(0f, 25f, 50f, 75f, 100f)

    // 레이블을 표시할 Y 값 반환
    override fun getLabelValues(
        context: ChartDrawContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical
    ): List<Float> {
        return fixedValues
    }

    // 레이블 높이 측정을 위해 사용할 Y 값 반환 (getLabelValues와 동일하게)
    override fun getHeightMeasurementLabelValues(
        context: MeasureContext,
        position: AxisPosition.Vertical
    ): List<Float> {
        return fixedValues
    }

    // 레이블 너비 측정을 위해 사용할 Y 값 반환 (getLabelValues와 동일하게)
    override fun getWidthMeasurementLabelValues(
        context: MeasureContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical
    ): List<Float> {
        return fixedValues
    }

    // 눈금(Tick) 및 가이드라인을 표시할 Y 값 반환 (null이면 getLabelValues 값을 사용)
    // 여기서는 레이블과 동일한 위치에 라인을 그리기 위해 null 반환 (또는 fixedValues 직접 반환)
    override fun getLineValues(
        context: ChartDrawContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical
    ): List<Float>? {
        return fixedValues // 명시적으로 같은 값을 반환해도 됨
    }

    // 축 상단에 필요한 여백 계산 (레이블 높이의 절반 정도)
    override fun getTopVerticalAxisInset(
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float
    ): Float {
        return when (verticalLabelPosition) {
            // 레이블이 라인 위에 있거나 중앙 정렬이면 높이의 절반만큼 여백 필요
            VerticalAxis.VerticalLabelPosition.Top,
            VerticalAxis.VerticalLabelPosition.Center -> maxLabelHeight / 2f
            // 레이블이 라인 아래에 있으면 여백 필요 없음
            VerticalAxis.VerticalLabelPosition.Bottom -> 0f
        }
    }

    // 축 하단에 필요한 여백 계산 (레이블 높이의 절반 정도)
    override fun getBottomVerticalAxisInset(
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float
    ): Float {
        return when (verticalLabelPosition) {
            // 레이블이 라인 아래에 있거나 중앙 정렬이면 높이의 절반만큼 여백 필요
            VerticalAxis.VerticalLabelPosition.Bottom,
            VerticalAxis.VerticalLabelPosition.Center -> maxLabelHeight / 2f
            // 레이블이 라인 위에 있으면 여백 필요 없음
            VerticalAxis.VerticalLabelPosition.Top -> 0f
        }
    }

    // (선택 사항) 최상단 라인 처리 방식 (기본값 true 사용)
    // override fun getShiftTopLines(chartDrawContext: ChartDrawContext): Boolean = true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YogaAccuracyTimeChart(
    yogaHistoryList: List<YogaHistory>,
    modifier: Modifier = Modifier
) {
    // 1. 데이터 Producer 및 정렬된 리스트 가져오기
    val (chartModelProducer, sortedHistory) = rememberYogaHistoryChartProducer(yogaHistoryList)

    // 2. X축 (Bottom Axis) 설정 - 날짜/시간 포매터
    //    차트 라이브러리가 오래된 버전이라면 java.text.SimpleDateFormat 사용
    //    val xDateFormatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    //    최신 자바 시간 API 사용 권장
    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.getDefault()) }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, chartValues ->
        // value는 ChartEntry의 x값 (여기서는 인덱스)
        val index = value.toInt()
        // 해당 인덱스의 createdAt 타임스탬프 찾기
        sortedHistory.getOrNull(index)?.createdAt?.let { timestamp ->
            try {
                // Long 타임스탬프를 LocalDateTime으로 변환 후 포맷팅
                val localDateTime = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                localDateTime.format(dateTimeFormatter)
            } catch (e: Exception) {
                // 변환 중 오류 발생 시 타임스탬프 원본 또는 빈 문자열 반환
                println("Error formatting timestamp: $timestamp, Error: ${e.message}")
                timestamp.toString() // 오류 시 원본 Long 값 표시 (혹은 빈 문자열 "")
            }
        } ?: "" // 해당 인덱스에 데이터가 없거나 createdAt이 null이면 빈 문자열 반환
    }

    val bottomAxis = rememberBottomAxis(
        valueFormatter = bottomAxisValueFormatter,
        // 레이블이 많으면 겹칠 수 있으므로, 레이블 회전이나 표시 간격 조절 고려
        // labelRotationDegrees = 45f,
        // guideline = null, // 가이드라인 제거
        // title = "Time"
    )

    // 3. Y축 (Start Axis) 설정 - 정확도 포매터
    val startAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, chartValues ->
        // value는 ChartEntry의 y값 (accuracy)
        "${(value)}%" // 예: 85.5 -> "85%" (소수점 버림) 또는 "%.1f%%".format(value)로 소수점 표시
    }
    val fixedStepPlacer = remember { FixedStepVerticalAxisPlacer() }

    val startAxis = rememberStartAxis(
        valueFormatter = startAxisValueFormatter,
        itemPlacer = fixedStepPlacer, // <- 여기에 커스텀 Placer 인스턴스 전달
    // guideline = ...
    )

    // 데이터가 없을 경우 차트 대신 다른 UI 표시 (선택 사항)
    if (sortedHistory.isEmpty()) {
        // 예: Text("표시할 요가 기록 데이터가 없습니다.")
        return // 차트를 그리지 않음
    }

    // 4. 차트 그리기
    Chart(
        modifier = modifier
            .height(250.dp)
            .padding(start = 16.dp, bottom = 16.dp, end = 8.dp, top = 8.dp),
        chart = lineChart(
            // 3개의 LineSpec 리스트를 전달 (Producer의 시리즈 순서와 일치해야 함)
            lines = listOf(
                // Spec 1: 실제 데이터용 (보이는 스타일)
                LineChart.LineSpec(
                    lineColor = PrimaryColor.toArgb(), // PrimaryColor 또는 원하는 색상
                    lineThicknessDp = 2f,
                    // 포인트, 데이터 라벨 등 필요시 여기에 추가
                ),
                // Spec 2: Y=0 경계선용 (투명 스타일)
                LineChart.LineSpec(
                    lineColor = Color.Transparent.toArgb(), // 완전 투명
                    lineThicknessDp = 0f, // 두께 0 또는 매우 작게
                    point = null, // 데이터 포인트 표시 안함
                    dataLabel = null, // 데이터 라벨 표시 안함
                    lineBackgroundShader = null // 배경 채우기 안함
                ),
                // Spec 3: Y=100 경계선용 (투명 스타일)
                LineChart.LineSpec(
                    lineColor = Color.Transparent.toArgb(), // 완전 투명
                    lineThicknessDp = 0f,
                    point = null,
                    dataLabel = null,
                    lineBackgroundShader = null
                )
            )
        ),
        chartModelProducer = chartModelProducer, // 3개 시리즈 포함된 Producer 전달
        startAxis = startAxis,
    )
}