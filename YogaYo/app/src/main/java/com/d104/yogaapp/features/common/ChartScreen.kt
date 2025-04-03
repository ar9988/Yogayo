package com.d104.yogaapp.features.common

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.d104.domain.model.YogaHistory
import com.d104.yogaapp.ui.theme.Neutral50
import com.d104.yogaapp.ui.theme.PastelBlue
import com.d104.yogaapp.ui.theme.PastelLigtBlue
import com.d104.yogaapp.ui.theme.PrimaryColor
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.marker.markerComponent
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
//import com.patrykandpatrick.vico.compose.marker.rememberMarker // *** 중요: rememberMarker 임포트 ***
import com.patrykandpatrick.vico.core.marker.Marker // Marker 타입 임포트
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@Composable
fun YogaAccuracyTimeChart(
    yogaHistoryList: List<YogaHistory>,
    modifier: Modifier = Modifier,
    accuracyColor: Color = PrimaryColor,
    poseTimeColor: Color = PastelLigtBlue
) {
    var selectedYAxisType by remember { mutableStateOf(YAxisType.ACCURACY) }



    // --- 데이터 준비 ---
    val (chartModelProducer, sortedHistory) = rememberYogaHistoryChartProducer(
        yogaHistoryList = yogaHistoryList,
        selectedYAxisType = selectedYAxisType // 선택된 Y축 타입 전달
    )

    // 2. X축 (Bottom Axis) 설정 - 날짜/시간 포매터
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

    val startAxisValueFormatter = remember(selectedYAxisType) {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, chartValues ->
            when (selectedYAxisType) {
                YAxisType.ACCURACY -> "${value.toInt()}%" // 정확도: % 표시
                YAxisType.POSE_TIME -> "%.1fs".format(Locale.US, value) // 포즈 시간: 초(s) 표시 (소수점 1자리)
            }
        }
    }
    val dynamicFixedStepPlacer = remember(selectedYAxisType) {
        FixedStepVerticalAxisPlacer(selectedYAxisType)
    }
    val startAxis = rememberStartAxis(
        valueFormatter = startAxisValueFormatter,
        itemPlacer = dynamicFixedStepPlacer,
        title = when (selectedYAxisType) { // Y축 제목 변경 (선택 사항)
            YAxisType.ACCURACY -> "정확도 (%)"
            YAxisType.POSE_TIME -> "유지 시간 (초)"
        }
    )

    // 데이터가 없을 경우 차트 대신 다른 UI 표시 (선택 사항)
    if (sortedHistory.isEmpty()) {
        // 예: Text("표시할 요가 기록 데이터가 없습니다.")
        return // 차트를 그리지 않음
    }

    val lineSpecs = remember(selectedYAxisType, accuracyColor, poseTimeColor) {
        when (selectedYAxisType) {
            YAxisType.ACCURACY -> listOf(
                // Spec 1: 실제 정확도 데이터 (색상 적용)
                LineChart.LineSpec(lineColor = accuracyColor.toArgb(), lineThicknessDp = 2f),
                // Spec 2 & 3: 0%, 100% 경계선 (투명)
                LineChart.LineSpec(lineColor = Color.Transparent.toArgb(), lineThicknessDp = 0f),
                LineChart.LineSpec(lineColor = Color.Transparent.toArgb(), lineThicknessDp = 0f)
            )
            YAxisType.POSE_TIME -> listOf(
                // Spec 1: 실제 포즈 시간 데이터 (다른 색상 적용)
                LineChart.LineSpec(lineColor = poseTimeColor.toArgb(), lineThicknessDp = 2f),
                LineChart.LineSpec(lineColor = Color.Transparent.toArgb(), lineThicknessDp = 0f),
                LineChart.LineSpec(lineColor = Color.Transparent.toArgb(), lineThicknessDp = 0f)
            )
        }
    }
    // 4. 차트 그리기
    Column(modifier = modifier.padding(horizontal = 16.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, end = 4.dp), // 오른쪽 끝에 약간의 패딩 추가 가능
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End // 스위치를 오른쪽 끝으로 정렬
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = "정확도",
                modifier = Modifier.size(24.dp),
                tint = if (selectedYAxisType == YAxisType.ACCURACY) accuracyColor else Neutral50
            )

            // 토글 스위치
            Switch(
                checked = selectedYAxisType == YAxisType.POSE_TIME,
                onCheckedChange = { isChecked ->
                    // 스위치 상태 변경 시 selectedYAxisType 업데이트
                    selectedYAxisType = if (isChecked) YAxisType.POSE_TIME else YAxisType.ACCURACY
                },
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = accuracyColor,
                    uncheckedBorderColor = accuracyColor,
                    checkedTrackColor = poseTimeColor,
                    uncheckedThumbColor = Color.White
                ),
                thumbContent = {
                    Box(
                        modifier = Modifier
                            .size(24.dp) // 고정 크기 지정
                            .background(color = Color.White, shape = CircleShape) // 항상 흰색 배경
                    )
                }
            )

            // "유지 시간" 텍스트 라벨
            //Icons.Outlined.Timer
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = "정확도",
                modifier = Modifier.size(24.dp),
                tint = if (selectedYAxisType == YAxisType.POSE_TIME) PastelBlue else Neutral50
            )
        }


        // 2. 차트 그리기

        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(start = 4.dp, bottom = 16.dp, end = 4.dp, top = 8.dp), // Column 패딩 고려
            chart = lineChart(
                lines = lineSpecs // 동적으로 생성된 LineSpec 리스트 전달
            ),
            chartModelProducer = chartModelProducer,
            startAxis = startAxis, // 동적으로 설정된 Y축
        )
    }
}



//
@Composable
fun rememberYogaHistoryChartProducer(
    yogaHistoryList: List<YogaHistory>,
    selectedYAxisType: YAxisType
): Pair<ChartEntryModelProducer, List<YogaHistory>> {

    val modelProducer = remember { ChartEntryModelProducer() }
    // 1. 원본 데이터 정렬 (X축 포매터용)
    val sortedHistory = remember(yogaHistoryList) {
        yogaHistoryList
            .filter { it.createdAt != null }
            .sortedBy { it.createdAt }
    }

    // 2. 시리즈 1: 실제 데이터 ChartEntry 리스트
    val realChartEntries: List<ChartEntry> = remember(sortedHistory, selectedYAxisType) {
        sortedHistory.mapIndexed { index, history ->
            val yValue = when (selectedYAxisType) {
                YAxisType.ACCURACY -> history.accuracy * 100 // 정확도는 백분율로
                YAxisType.POSE_TIME -> history.poseTime     // 포즈 시간은 초 단위 그대로
            }
            entryOf(index.toFloat(), yValue)
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
            // 시작과 끝 X 지점에 Y의 최댓 값 추가
            when (selectedYAxisType) {
                YAxisType.ACCURACY -> listOf(entryOf(minX, 100f), entryOf(maxX, 100f))
                YAxisType.POSE_TIME -> listOf(entryOf(minX, 20f), entryOf(maxX, 20f))
            }

        } else {
            emptyList()
        }
    }

    // 4. 3개의 시리즈를 포함하는 ChartEntryModel 생성 및 Producer 업데이트
    LaunchedEffect(realChartEntries, boundaryEntries0, boundaryEntries100, selectedYAxisType) {
        val animationSpec = tween<Float>(durationMillis = 300)
        if (realChartEntries.isNotEmpty()) {
            val entriesToSet = listOf(realChartEntries, boundaryEntries0, boundaryEntries100)
            println("Updating chart producer for $selectedYAxisType with ${entriesToSet.size} series.")
            modelProducer.setEntriesSuspending(entriesToSet)
        } else {
            modelProducer.setEntries(emptyList<List<ChartEntry>>()) // 데이터 없으면 비우기
        }
    }

    // Producer와 정렬된 '원본' 데이터 리스트 반환 (X축 포매터용)
    return remember(modelProducer, sortedHistory) { modelProducer to sortedHistory }
}


class FixedStepVerticalAxisPlacer(yAxisType:YAxisType) : AxisItemPlacer.Vertical {

    // 표시할 고정된 Y축 값 리스트
    private val fixedValues = if(yAxisType==YAxisType.ACCURACY)listOf(0f, 25f, 50f, 75f, 100f)else listOf(0f,5f,10f,15f,20f)

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

fun formatTimestampForMarker(timestamp: Long?): String {
    if (timestamp == null) return "시간 정보 없음"
    return try {
        // 예: "MM/dd HH:mm" 또는 필요에 따라 다른 형식 사용
        val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "시간 형식 오류"
    }
}


enum class YAxisType {
    ACCURACY, POSE_TIME
}

