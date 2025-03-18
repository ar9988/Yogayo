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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
import com.d104.yogaapp.ui.theme.Neutral70
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomCourseDialog(
    originalCourseName:String = "",
    poseInCourse:List<YogaPoseInCourse> = emptyList(),
    poseList: List<YogaPose>,
    onDismiss: () -> Unit,
    onSave: (String,List<YogaPoseWithOrder>) -> Unit
) {
    val context = LocalContext.current
    // 상태 관리
    var courseName by remember { mutableStateOf(originalCourseName) }
    var searchQuery by remember { mutableStateOf("") }
    val allPoses = remember { poseList}

    val selectedPoses = remember { poseInCourse.toMutableStateList() }
    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()
    val dragAndDropListState = rememberDragAndDropListState(lazyListState) { from, to ->
        selectedPoses.move(from, to)
    }

    var overscrollJob by remember { mutableStateOf<Job?>(null) }


    // 검색 결과 필터링
    val filteredPoses = remember(searchQuery, allPoses) {
        if (searchQuery.isEmpty()) {
            allPoses
        } else {
            allPoses.filter { it.poseName.contains(searchQuery, ignoreCase = true) }
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
                                        detectDragGesturesAfterLongPress(
                                            onDrag = { change, offset ->
                                                change.consume()
                                                dragAndDropListState.onDrag(offset)

                                                if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                                                dragAndDropListState
                                                    .checkOverscroll()
                                                    .takeIf { it != 0f }
                                                    ?.let {
                                                        overscrollJob = coroutineScope.launch {
                                                            dragAndDropListState.lazyListState.scrollBy(
                                                                it
                                                            )
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(
                                    items = selectedPoses,
                                    key = { _, item -> item.uniqueID }
                                ) { index, item ->
                                    val isDragging =
                                        index == dragAndDropListState.currentIndexOfDraggedItem
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .animateItemPlacement(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable(onClick = { selectedPoses.removeAt(index) })
                                                .border(
                                                    width = if (isDragging) 3.dp else 0.dp,
                                                    color = if (isDragging) Color(0xFFF9A8A8) else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 2.dp
                                            )
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(item.pose.poseImg)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = item.pose.poseName,
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
                                                        .padding(2.dp),
                                                    tint = Color.Black
                                                )
                                            }
                                        }
                                        if (index < selectedPoses.size - 1) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_arrow_right),
                                                contentDescription = "다음",
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .height(24.dp)
                                            )
                                        }
                                    }
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
                            .background(Color(0xFFF0F0FA), RoundedCornerShape(24.dp)),
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredPoses) { pose ->
                            YogaPoseCard(
                                pose = pose,
                                onClick = {
                                    if(selectedPoses.size>=10){
                                        Toast.makeText(context, "자세는 10개까지 가능합니다", Toast.LENGTH_SHORT).show()
                                    }else{
                                    val uniqueId = "${pose.poseId}-${System.currentTimeMillis()}"
                                    val newOrderIndex = selectedPoses.size // 새 항목은 맨 뒤에 추가
                                    selectedPoses.add(YogaPoseInCourse(uniqueID = uniqueId, pose = pose))
                                    Timber.d("Added new pose: ${pose.poseName} with orderIndex $newOrderIndex")
                                    Timber.d("${selectedPoses.toList()}")
                                        }
                                }
                            )

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
                        }else {
                            onSave(courseName, selectedPoses.mapIndexed { index, poseInCourse ->
                                YogaPoseWithOrder(
                                    userOrderIndex = index,
                                    poseId = poseInCourse.pose.poseId
                                )
                            })
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
            .clickable(onClick = onClick),
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
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
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

fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) return
    val element = this.removeAt(from)
    this.add(to, element)
}
class DragAndDropListState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    // 상태 변수들
    private var draggingDistance by mutableFloatStateOf(0f)
    private var initialDraggingElement by mutableStateOf<LazyListItemInfo?>(null)
    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    // LazyListState에서 특정 위치의 아이템 정보 가져오기
    private fun LazyListState.getVisibleItemInfo(itemPosition: Int): LazyListItemInfo? {
        return this.layoutInfo.visibleItemsInfo.getOrNull(itemPosition - this.firstVisibleItemIndex)
    }

    // 아이템의 끝 오프셋 계산
    private val LazyListItemInfo.offsetEnd: Int
        get() = this.offset + this.size

    // 초기 오프셋 계산
    private val initialOffsets: Pair<Int, Int>?
        get() = initialDraggingElement?.let { Pair(it.offset, it.offsetEnd) }

    // 현재 드래그 중인 아이템의 변위 계산
    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem?.let {
            lazyListState.getVisibleItemInfo(it)
        }?.let { itemInfo ->
            (initialDraggingElement?.offset ?: 0f).toFloat() + draggingDistance - itemInfo.offset
        }

    // 현재 드래그 중인 아이템 정보
    private val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let {
            lazyListState.getVisibleItemInfo(it)
        }

    // 드래그 시작 시 호출
    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.x.toInt() in item.offset..item.offsetEnd }
            ?.also {
                initialDraggingElement = it
                currentIndexOfDraggedItem = it.index
            }
    }

    // 드래그 중단 시 호출
    fun onDragInterrupted() {
        initialDraggingElement = null
        currentIndexOfDraggedItem = null
        draggingDistance = 0f
    }

    // 드래그 중 호출
    fun onDrag(offset: Offset) {
        draggingDistance += offset.x * 0.8f
        initialOffsets?.let { (start, end) ->
            val startOffset = start.toFloat() + draggingDistance
            val endOffset = end.toFloat() + draggingDistance
            currentElement?.let { current ->
                lazyListState.layoutInfo.visibleItemsInfo
                    .filterNot { item ->
                        item.offsetEnd < startOffset || item.offset > endOffset || current.index == item.index
                    }
                    .firstOrNull { item ->
                        val delta = startOffset - current.offset
                        when {
                            delta < 0 -> item.offset > startOffset
                            else -> item.offsetEnd < endOffset
                        }
                    }?.also { item ->
                        currentIndexOfDraggedItem?.let { current ->
                            onMove.invoke(current, item.index)
                            currentIndexOfDraggedItem = item.index
                        }
                    }
            }
        }
    }

    // 오버스크롤 체크
    fun checkOverscroll(): Float {
        return initialDraggingElement?.let {
            val startOffset = it.offset + draggingDistance
            val endOffset = it.offsetEnd + draggingDistance
            return@let when {
                draggingDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                draggingDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
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
