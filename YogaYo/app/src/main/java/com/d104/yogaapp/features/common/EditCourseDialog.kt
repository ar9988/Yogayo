package com.d104.yogaapp.features.common

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseInCourse
import com.d104.yogaapp.R
import com.d104.yogaapp.ui.theme.Neutral50
import com.d104.yogaapp.ui.theme.Neutral70
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomCourseDialog(
    originalCourseName:String = "",
    isLoading:Boolean = false,
    poseInCourse:List<YogaPoseInCourse> = emptyList(),
    poseList: List<YogaPose>,
    onDismiss: () -> Unit,
    onSave: (String,List<YogaPoseWithOrder>) -> Unit,
    isMultiMode: Boolean = false,
    onAdd: (String, List<YogaPose>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    // 상태 관리
    var courseName by remember { mutableStateOf(originalCourseName) }
    var searchQuery by remember { mutableStateOf("") }

    val selectedPoses = remember { poseInCourse.toMutableStateList() }
    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()
    val dragAndDropListState = rememberDragAndDropListState(lazyListState) { from, to ->
        selectedPoses.move(from, to)
    }

    var overscrollJob by remember { mutableStateOf<Job?>(null) }


    // 검색 결과 필터링
    val filteredPoses = remember(searchQuery, poseList) {
        if (searchQuery.isEmpty()) {
            poseList
        } else {
            poseList.filter { it.poseName.contains(searchQuery, ignoreCase = true) }
        }
    }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.Black
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top= 32.dp)
                ) {
                    // 코스 이름 입력
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text(style = TextStyle(color = Neutral70), text = "코스 이름") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        maxLines = 1,
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            containerColor = Color(0xFFF5F5F5)
                        )
                    )

                    // 선택된 요가 포즈 목록 (드래그 가능)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        if (selectedPoses.isEmpty()) {
                            // 빈 상태일 때 안내 메시지 표시
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "자세 추가",
                                        tint = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "아래에서 자세를 선택하세요",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        var overscrollJob: kotlinx.coroutines.Job? = null

                                        detectDragGesturesAfterLongPress(
                                            onDrag = { change, offset ->
                                                change.consume()
                                                dragAndDropListState.onDrag(offset)

                                                if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                                                // 스크롤 체크 및 처리
                                                dragAndDropListState.checkOverscroll()
                                                    .takeIf { it != 0f }
                                                    ?.let {
                                                        overscrollJob = coroutineScope.launch {
                                                            dragAndDropListState.lazyListState.scrollBy(it)
                                                        }
                                                    } ?: run { overscrollJob?.cancel() }
                                            },
                                            onDragStart = { offset ->
                                                dragAndDropListState.onDragStart(offset)
                                            },
                                            onDragEnd = { dragAndDropListState.onDragInterrupted() },
                                            onDragCancel = { dragAndDropListState.onDragInterrupted() }
                                        )
                                    },
                                state = dragAndDropListState.lazyListState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                            ) {
                                itemsIndexed(
                                    items = selectedPoses,
                                    key = { _, item -> item.uniqueID }
                                ) { index, item ->
                                    val isDragging = index == dragAndDropListState.currentIndexOfDraggedItem
                                    val displacement = if (isDragging) {
                                        dragAndDropListState.elementDisplacement ?: 0f
                                    } else 0f

                                    // 드래그 중인 아이템에는 애니메이션을 적용하지 않음
                                    val itemModifier = if (isDragging) {
                                        Modifier
                                            .offset { IntOffset(displacement.roundToInt(), 0) }
                                            .zIndex(1f) // 드래그 중인 아이템을 항상 위에 표시
                                    } else {
                                        Modifier
                                            .zIndex(0f) // 일반 아이템은 기본 zIndex
                                            .animateItemPlacement(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                    }

                                    PoseInCourseItem(
                                        pose = item.pose,
                                        onDeleteClick = { selectedPoses.removeAt(index) },
                                        showArrow = index < selectedPoses.size - 1,
                                        isDragging = isDragging,
                                        modifier = itemModifier
                                    )
                                }
                            }
                        }
                    }

                    // 검색창
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(style = TextStyle(color = Neutral70), text = "요가 자세 검색") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp)),
                        maxLines = 1,
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "검색"
                            )
                        }
                    )

                    // 요가 포즈 그리드
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }

                    }else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                        ) {
                            items(filteredPoses) { pose ->
                                YogaPoseCard(
                                    pose = pose,
                                    onClick = {
                                        if (selectedPoses.size >= 10) {
                                            Toast.makeText(
                                                context,
                                                "자세는 10개까지 가능합니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            val uniqueId =
                                                "${pose.poseId}-${System.currentTimeMillis()}"
                                            val newOrderIndex = selectedPoses.size // 새 항목은 맨 뒤에 추가
                                            selectedPoses.add(
                                                YogaPoseInCourse(
                                                    uniqueID = uniqueId,
                                                    pose = pose
                                                )
                                            )
                                            Timber.d("Added new pose: ${pose.poseName} with orderIndex $newOrderIndex")
                                            Timber.d("${selectedPoses.toList()}")
                                            coroutineScope.launch {
                                                delay(50)
                                                dragAndDropListState.lazyListState.scrollToItem(
                                                    selectedPoses.size
                                                )
                                            }
                                        }
                                    }
                                )

                            }
                        }
                    }

                    // 하단 여백 (버튼 공간 확보)
                    Spacer(modifier = Modifier.height(50.dp))
                }

                // 적용 버튼 (하단에 떠있음)
                Button(
                    onClick = {
                        // PoseInCourse에서 YogaPose만 추출하여 전달 (정렬된 순서대로)
                        if(courseName.equals("")){
                            Toast.makeText(context, "코스 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                        }else if(selectedPoses.isEmpty()){
                            Toast.makeText(context, "자세를 1개 이상 선택해주세요", Toast.LENGTH_SHORT).show()
                        }else if(!isMultiMode){
                            onSave(courseName, selectedPoses.mapIndexed { index, poseInCourse ->
                                YogaPoseWithOrder(
                                    userOrderIndex = index,
                                    poseId = poseInCourse.pose.poseId
                                )
                            })
                        }else{
                            val yogaPoseList = selectedPoses.map { it.pose }
                            onAdd(courseName, yogaPoseList)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9A8A8))
                ) {
                    Text(
                        text = "확인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
fun YogaPoseCard(
    pose: YogaPose,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
        ,
        colors = CardDefaults.cardColors(
            containerColor = Color.White // 카드의 배경색을 흰색으로 명시적 설정
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 요가 포즈 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // 샘플 이미지 (실제로는 네트워크 이미지 로드)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pose.poseImg)
                        .crossfade(true)
                        .build(),
                    contentDescription = pose.poseName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    // 로딩 중 표시할 플레이스홀더
                    placeholder = painterResource(R.drawable.ic_yoga),
                    // 에러 발생 시 표시할 이미지
                    error = painterResource(R.drawable.ic_yoga)
                )

                // 대칭 포즈 여부
                if (pose.setPoseId != -1L) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_mirror),
                            contentDescription = "Mirror icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 요가 포즈 이름
            Row (modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,


                ) {
                Text(
                    text = pose.poseName,
                    textAlign = TextAlign.Start,
                    fontSize = when {
                        pose.poseName.length > 10 -> 12.sp
                        pose.poseName.length > 8 -> 14.sp
                        else -> 16.sp
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 난이도 표시
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_yoga),
                        contentDescription = "DifficultyIcon",
                        tint = when (pose.poseLevel) {
                            1 -> Color(0xFF4CAF50) // 녹색
                            2 -> Color(0xFFFFC107) // 황색
                            else -> Color(0xFFF44336) // 적색
                        }


                    )
                    Text(
                        modifier = Modifier.offset(y = (-4).dp),
                        text = when (pose.poseLevel) {
                            1 -> "Easy"
                            2 -> "Medium"
                            else -> "Hard"
                        },
                        color = when (pose.poseLevel) {
                            1 -> Color(0xFF4CAF50) // 녹색
                            2 -> Color(0xFFFFC107) // 황색
                            else -> Color(0xFFF44336) // 적색
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



fun CoroutineScope.launchCatching(block: suspend () -> Unit) {
    launch {
        try {
            block()
        } catch (e: Exception) {
            // 애니메이션 취소 또는 기타 예외 처리
        }
    }
}

@Composable
fun PoseInCourseItem(
    pose: YogaPose,
    onDeleteClick: () -> Unit,
    showArrow: Boolean = false,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // 포즈 카드
        Card(
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .border(
                    width = if (isDragging) 3.dp else 0.dp,
                    color = if (isDragging) Color(0xFFF9A8A8) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDragging) 8.dp else 2.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pose.poseImg)
                        .crossfade(true)
                        .build(),
                    contentDescription = pose.poseName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_yoga),
                    error = painterResource(R.drawable.ic_yoga)
                )

                // 삭제 아이콘
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "삭제",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .clickable(onClick = onDeleteClick),
                    tint = Color.Black
                )
            }
        }

        // 화살표 (조건부 표시)
        if (showArrow) {
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "다음",
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
                    .alpha(if (isDragging) 0f else 1f) // 드래그 중에는 화살표 숨김
            )
        }
    }
}

fun <T> MutableList<T>.move(from: Int, to: Int) {
    Timber.d("${from}에서 ${to}로 이동")
    if (from == to) return

    // 드래그 방향에 따라 한 칸씩만 이동하도록 수정
    val targetIndex = if (from < to) {
        // 앞에서 뒤로 드래그할 때는 한 칸만 이동
        from + 1
    } else {
        // 뒤에서 앞으로 드래그할 때는 한 칸만 이동
        from - 1
    }

    // 실제 이동할 인덱스를 제한
    val actualTo = if (to == targetIndex) to else targetIndex

    // 이동 실행
    val element = this.removeAt(from)
    this.add(actualTo, element)
}
class DragAndDropListState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    // 상태 변수들
    private var initialDraggingElement by mutableStateOf<LazyListItemInfo?>(null)
    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    // 누적 드래그 거리
    private var cumulativeDragDelta by mutableFloatStateOf(0f)

    // 현재 손가락 위치
    private var currentDragPosition by mutableStateOf<Offset?>(null)

    // 드래그 시작 시 아이템 내 터치 위치
    private var touchPositionInItem by mutableFloatStateOf(0f)

    // 드래그 중인 아이템의 예상 위치
    private var targetItemPosition by mutableFloatStateOf(0f)

    // 위치 업데이트 스킵 플래그
    private var skipNextPositionUpdate by mutableStateOf(false)

    // 드래그 중인 아이템의 원래 크기
    private var draggedItemSize by mutableIntStateOf(0)

    // 마지막 스왑 시간
    private var lastSwapTime by mutableLongStateOf(0L)

    // 스왑 쿨다운
    private val swapCooldown = 300L

    // 스왑 방향 추적
    private var lastSwapDirection by mutableIntStateOf(0)

    // 자동 스크롤 코루틴 작업
    private var autoScrollJob by mutableStateOf<Job?>(null)

    // LazyListState에서 특정 위치의 아이템 정보 가져오기
    private fun LazyListState.getVisibleItemInfo(itemIndex: Int): LazyListItemInfo? {
        return this.layoutInfo.visibleItemsInfo.firstOrNull { it.index == itemIndex }
    }

    // 아이템의 끝 오프셋 계산
    private val LazyListItemInfo.offsetEnd: Int
        get() = this.offset + this.size

    // 현재 드래그 중인 아이템의 변위 계산
    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem?.let { currentIndex ->
            val itemInfo = lazyListState.getVisibleItemInfo(currentIndex)

            if (itemInfo != null) {
                return@let currentDragPosition?.x?.minus(touchPositionInItem)?.minus(itemInfo.offset) ?: 0f
            } else {
                // 아이템이 화면 밖으로 나갔을 때: 자동 스크롤 트리거
                ensureDraggedItemVisible(currentIndex)
                return@let targetItemPosition
            }
        }

    // 드래그 중인 아이템이 화면에 보이도록 함
    private fun ensureDraggedItemVisible(index: Int) {
        autoScrollJob?.cancel()
        autoScrollJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                // 드래그 중인 아이템으로 스크롤
                lazyListState.animateScrollToItem(index = index, scrollOffset = 50)
                delay(10) // 스크롤 완료 대기

                // 아이템이 보이는지 다시 확인
                val newItemInfo = lazyListState.getVisibleItemInfo(index)
                if (newItemInfo != null) {
                    // 위치 재조정
                    currentDragPosition?.let { pos ->
                        targetItemPosition = pos.x - touchPositionInItem - newItemInfo.offset
                    }
                }
            } catch (e: Exception) {
                // 스크롤 중 오류 처리
            }
        }
    }

    // 드래그 시작 시 호출
    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.x.toInt() in item.offset..item.offsetEnd }
            ?.also {
                initialDraggingElement = it
                currentIndexOfDraggedItem = it.index
                draggedItemSize = it.size

                currentDragPosition = offset
                touchPositionInItem = offset.x - it.offset
                targetItemPosition = 0f
                cumulativeDragDelta = 0f
                skipNextPositionUpdate = false
                lastSwapTime = 0L
                lastSwapDirection = 0

                autoScrollJob?.cancel()
                autoScrollJob = null
            }
    }

    // 드래그 중단 시 호출
    fun onDragInterrupted() {
        initialDraggingElement = null
        currentIndexOfDraggedItem = null
        currentDragPosition = null
        cumulativeDragDelta = 0f
        targetItemPosition = 0f
        skipNextPositionUpdate = false
        lastSwapTime = 0L
        lastSwapDirection = 0

        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    // 드래그 중 호출
    fun onDrag(offset: Offset) {
        val previousPosition = currentDragPosition ?: offset

        currentDragPosition = Offset(
            previousPosition.x + offset.x,
            previousPosition.y + offset.y
        )

        cumulativeDragDelta += offset.x

        currentIndexOfDraggedItem?.let { currentIndex ->
            val fingerPosition = currentDragPosition?.x ?: return@let

            // 현재 아이템이 화면에 보이는지 확인
            val currentItemInfo = lazyListState.getVisibleItemInfo(currentIndex)

            // 아이템이 보이지 않으면 바로 스크롤하여 보이게 함
            if (currentItemInfo == null) {
                ensureDraggedItemVisible(currentIndex)
                return@let
            }

            if (!skipNextPositionUpdate) {
                targetItemPosition = fingerPosition - touchPositionInItem - currentItemInfo.offset
            }

            skipNextPositionUpdate = false

            // 현재 시간 및 스왑 허용 여부 확인
            val currentTime = System.currentTimeMillis()
            val canSwap = currentTime - lastSwapTime > swapCooldown
            Timber.d("current:${currentTime}, lastTIme:${lastSwapTime}}")
            if (!canSwap) return@let

            // 오직 화면에 보이는 아이템과만 스왑
            lazyListState.layoutInfo.visibleItemsInfo
                .filter { it.index != currentIndex }
                .forEach { item ->
                    val moveRight = currentIndex < item.index && item.index == currentIndex + 1
                    val moveLeft = currentIndex > item.index && item.index == currentIndex - 1

                    if (!moveRight && !moveLeft) return@forEach

                    val swapThreshold = item.size * 0.33f

                    if ((moveRight && fingerPosition > item.offset + swapThreshold) ||
                        (moveLeft && fingerPosition < item.offsetEnd - swapThreshold)) {

                        val currentDirection = if (moveRight) 1 else -1
                        if (lastSwapDirection != 0 && lastSwapDirection != currentDirection) {
                            if (moveRight && fingerPosition < item.offset + item.size * 0.75f) return@forEach
                            if (moveLeft && fingerPosition > item.offsetEnd - item.size * 0.75f) return@forEach
                        }


                        // 스왑 전 위치 계산
                        val savedFingerPosition = fingerPosition

                        skipNextPositionUpdate = true
                        lastSwapTime = currentTime  // 여기서 미리 설정
                        lastSwapDirection = currentDirection

                        // 아이템 교환
                        onMove.invoke(currentIndex, item.index)
                        currentIndexOfDraggedItem = item.index

                        // 비동기 처리를 동기화
                        val mainScope = CoroutineScope(Dispatchers.Main)
                        mainScope.launch {
                            // 레이아웃 업데이트 대기 시간을 좀 더 늘려볼 수 있습니다
                            delay(50)  // 30ms에서 50ms로 증가

                            // UI 갱신 후 정보 가져오기
                            lazyListState.getVisibleItemInfo(item.index)?.let { newItem ->
                                targetItemPosition = savedFingerPosition - touchPositionInItem - newItem.offset
                                // 작업 완료 후 상태 재확인을 위한 플래그 설정 가능
                                // skipNextPositionUpdate = false  // 필요시 여기서 다시 false로 설정
                            }
                        }

                        return@forEach
                    }
                }
        }
    }

    // 오버스크롤 체크 - 스크롤 가속도를 조절하여 부드럽게 스크롤
    fun checkOverscroll(): Float {
        return currentDragPosition?.let { position ->
            val viewportStart = lazyListState.layoutInfo.viewportStartOffset
            val viewportEnd = lazyListState.layoutInfo.viewportEndOffset
            val scrollSensitivity = 12f

            return@let when {
                position.x > viewportEnd - 120 -> {
                    val distance = (position.x - (viewportEnd - 120)).coerceIn(0f, 120f)
                    (distance / 120f) * scrollSensitivity
                }
                position.x < viewportStart + 120 -> {
                    val distance = ((viewportStart + 120) - position.x).coerceIn(0f, 120f)
                    -(distance / 120f) * scrollSensitivity
                }
                else -> 0f
            }
        } ?: 0f
    }
}

@Composable
fun rememberDragAndDropListState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragAndDropListState {
    return remember { DragAndDropListState(lazyListState, onMove) }
}

// LazyRow에서 사용할 Modifier 확장 함수
fun Modifier.dragAndDropEnabled(
    state: DragAndDropListState,
    coroutineScope: CoroutineScope
): Modifier {
    return this.pointerInput(Unit) {
        var overscrollJob: kotlinx.coroutines.Job? = null

        detectDragGesturesAfterLongPress(
            onDrag = { change, offset ->
                change.consume()
                state.onDrag(offset)

                if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                state.checkOverscroll()
                    .takeIf { it != 0f }
                    ?.let {
                        overscrollJob = coroutineScope.launch {
                            state.lazyListState.scrollBy(it)
                        }
                    } ?: run { overscrollJob?.cancel() }
            },
            onDragStart = { offset ->
                state.onDragStart(offset)
            },
            onDragEnd = { state.onDragInterrupted() },
            onDragCancel = { state.onDragInterrupted() }
        )
    }
}

//// 드래그 모디파이어
fun Modifier.dragModifier(index: Int, dragAndDropListState: DragAndDropListState) = composed {
    val isDragging = index == dragAndDropListState.currentIndexOfDraggedItem
    val offsetOrNull = dragAndDropListState.elementDisplacement.takeIf { isDragging }

    Modifier
        .zIndex(if (isDragging) 1f else 0f)
        .graphicsLayer {
            translationX = offsetOrNull ?: 0f
            scaleX = if (isDragging) 1.05f else 1f
            scaleY = if (isDragging) 1.05f else 1f
        }
}
