package com.d104.yogaapp.features.solo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.domain.model.UserCourse
import com.d104.yogaapp.R
import com.d104.yogaapp.ui.theme.PrimaryColor
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.yogaapp.features.common.CourseCard
import com.d104.yogaapp.features.common.CustomCourseDialog
import com.d104.yogaapp.ui.theme.SplashFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SoloScreen(
    viewModel: SoloViewModel = hiltViewModel(),
    isLogin:Boolean = false,
    onNavigateToYogaPlay: (UserCourse) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedCourse by remember { mutableStateOf<UserCourse?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 코스 시작 다이얼로그 표시
    if(state.showAddCourseDialog){
        CustomCourseDialog(
            poseList = state.yogaPoses,
            isLoading = state.yogaPoseLoading,
            onDismiss = { viewModel.handleIntent(SoloIntent.HideAddCourseDialog) },
            onSave = { courseName,poses ->
                viewModel.handleIntent(SoloIntent.CreateCourse(courseName, poses))
                viewModel.handleIntent(SoloIntent.HideAddCourseDialog)
                coroutineScope.launch {
                    delay(300)
                    listState.animateScrollToItem(state.courses.size)
                }
            }
        )
    }
    selectedCourse?.let { course ->
        YogaCourseStartDialog(
            course = course,
            onDismiss = { selectedCourse = null },
            onConfirm = { updatedCourse ->
                // 튜토리얼 상태가 변경되었으면 코스 업데이트
                if (updatedCourse.tutorial != course.tutorial) {
                    viewModel.handleIntent(
                        SoloIntent.UpdateCourseTutorial(
                            updatedCourse,
                            updatedCourse.tutorial
                        )
                    )
                }
                selectedCourse = null
                onNavigateToYogaPlay(updatedCourse)
            }
        )
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "요가요",
                            color = PrimaryColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SplashFontFamily // 커스텀 폰트
                        )
                    }
                }

                items(
                    items = state.courses,
                    key = { course -> course.courseId }
                ) { course ->
                    if(course.courseId < 0) {
                        CourseCard(
                            header = { SoloCourseCardHeader(course) },
                            poseList = state.yogaPoses,
                            course = course,
                            onClick = { selectedCourse = course },
                            onUpdateCourse = { courseName, poses ->
                                viewModel.handleIntent(
                                    SoloIntent.UpdateCourse(
                                        course.courseId,
                                        courseName,
                                        poses
                                    )
                                )
                            },
                        )
                    } else {
//                        SwipeableCourseDismissBox(
//                            course = course,
//                            poseList = state.yogaPoses,
//                            onClick = { selectedCourse = course },
//                            onUpdateCourse = { courseName, poses ->
//                                viewModel.handleIntent(
//                                    SoloIntent.UpdateCourse(
//                                        course.courseId,
//                                        courseName,
//                                        poses
//                                    )
//                                )
//                            },
//                            onDeleteCourse = { courseToDelete ->
//                                viewModel.handleIntent(SoloIntent.DeleteCourse(courseToDelete.courseId))
//                            }
//                        )
                        CourseCard(
                            header = { SoloCourseCardHeader(course) },
                            poseList = state.yogaPoses,
                            onClick = { selectedCourse = course },
                            onUpdateCourse = { courseName, poses ->
                                viewModel.handleIntent(
                                    SoloIntent.UpdateCourse(
                                        course.courseId,
                                        courseName,
                                        poses
                                    )
                                )
                            },
                            onDeleteCourse = { courseToDelete ->
                                viewModel.handleIntent(SoloIntent.DeleteCourse(courseToDelete.courseId))
                            },
                            course = course

                        )
                    }
                }

                if (state.isLogin&&state.courses.size <= 8) {
                    item {
                        AddCourseButton(
                            onClick = {viewModel.handleIntent(SoloIntent.ShowAddCourseDialog)}
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun SoloCourseCardHeader(course: UserCourse){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 핵심 수정: 왼쪽 Row에 weight(1f) 적용 ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f) // 이 Row가 남은 공간을 모두 차지하도록 함
            ) {
                Text(
                    text = course.courseName,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // --- 내부 weight 수정: fill = false 유지 중요 ---
                    // 이 Text가 '튜토리얼' 태그 공간을 제외한 나머지 공간을 차지
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = if(course.tutorial == true) 16.sp else 24.sp
                )

                // 튜토리얼 여부 표시 (필요 시 공간 차지)
                if (course.tutorial == true) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryColor,
                        modifier = Modifier.height(26.dp) // 높이 고정
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "튜토리얼",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } // --- 왼쪽 Row 끝 ---

            // 오른쪽 요소(시간)를 위한 공간 확보를 위해 약간의 간격 추가
            Spacer(modifier = Modifier.width(8.dp)) // 왼쪽과 오른쪽 그룹 사이 간격

            // --- 오른쪽 Row는 고정 크기를 가짐 (weight 없음) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
                // Modifier에 weight 없음
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "예상 시간",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp) // 아이콘 크기 고정 (선택 사항)
                )

                // 각 포즈당 3분으로 계산
                val durationMinutes = course.poses.size * 3
                Text(
                    text = "${durationMinutes}분",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1 // 시간 텍스트도 한 줄로 제한 (혹시 모를 경우 대비)
                )
            } // --- 오른쪽 Row 끝 ---
        }

    }
}

@Composable
fun YogaCourseStartDialog(
    course: UserCourse,
    onDismiss: () -> Unit,
    onConfirm: (UserCourse) -> Unit // 변경된 코스 객체를 전달하는 콜백
) {
    // 튜토리얼 상태를 관리할 변수
    var isTutorial by remember { mutableStateOf(course.tutorial) }

    // 코스 객체를 복사하여 튜토리얼 상태가 변경된 버전을 생성
    val updatedCourse = remember(isTutorial) {
        course.copy(tutorial = isTutorial)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // 코스 시작 메시지
                Text(
                    text = "${course.courseName}을 시작 하시겠습니까?",
                    style = MaterialTheme.typography.titleMedium
                )

                // 튜토리얼 체크박스 - 이제 상호작용 가능
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "튜토리얼 보기",
                    )
                    Checkbox(
                        checked = isTutorial,
                        onCheckedChange = { isTutorial = it },
                        enabled = true,
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryColor,          // 체크됐을 때 배경 색상
                            checkmarkColor = Color.White,      // 체크마크 색상
                        )

                    )
                }

                // 버튼 행
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text(text = "취소",fontSize = 14.sp )
                    }

                    Button(
                        onClick = {
                            // 업데이트된 코스 정보로 확인 처리
                            onConfirm(updatedCourse)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text(text = "확인", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCourseDismissBox(
    course: UserCourse,
    poseList: List<YogaPose> = emptyList(),
    onClick: () -> Unit,
    onUpdateCourse: (String, List<YogaPoseWithOrder>) -> Unit,
    onDeleteCourse: (UserCourse) -> Unit
) {
    val canDelete = course.courseId >= 0 // 삭제 가능 여부 (ID가 0 이상일 때만)
    val isRemoved = remember(course.courseId) { mutableStateOf(false) }
    val animationCompleted = remember(course.courseId) { mutableStateOf(false) }

    // --- 추가: 삭제 확인 다이얼로그 표시 상태 ---
    var showDeleteDialog by remember(course.courseId) { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = { totalDistance -> totalDistance * 0.6f },
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart && canDelete) {
                // --- 수정: 바로 삭제하지 않고 다이얼로그 표시 요청 ---
                showDeleteDialog = true
                // --- 중요: SwipeToDismissBox의 상태 변경을 막음 (항상 false 반환) ---
                // 이렇게 해야 스와이프 후 원래 위치로 돌아오고, 다이얼로그가 뜬다.
                false
            } else {
                false // 다른 방향 스와이프나 삭제 불가능 시에도 상태 변경 안 함
            }
        }
    )

    // 삭제 애니메이션 및 실제 삭제 처리 (이 부분은 isRemoved 상태에 의존하므로 유지)
    LaunchedEffect(isRemoved.value) {
        if (isRemoved.value) {
            delay(300) // 애니메이션 시간과 맞춤
            animationCompleted.value = true
            onDeleteCourse(course) // 실제 데이터 삭제
        }
    }

    // --- 삭제 확인 다이얼로그 ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false }, // 다이얼로그 밖 클릭 시 닫기
            title = { Text("삭제 확인") },
            text = { Text("정말로 '${course.courseName}' 코스를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false // 다이얼로그 닫기
                        // --- 수정: 여기서 삭제 애니메이션 시작 및 실제 삭제 트리거 ---
                        isRemoved.value = true
                    },
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false } // 다이얼로그 닫기
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 애니메이션 완료 전 & 다이얼로그 확인 후 삭제 시작 시 사라지도록 렌더링
    if (!animationCompleted.value) {
        AnimatedVisibility(
            // isRemoved가 true가 되면 (다이얼로그에서 '삭제' 클릭 시) 사라짐 애니메이션 시작
            visible = !isRemoved.value,
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                    shrinkHorizontally(animationSpec = tween(durationMillis = 300), shrinkTowards = Alignment.Start)
        ) {
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false, // 오른쪽 -> 왼쪽 스와이프만 활성화
                enableDismissFromEndToStart = canDelete,
                backgroundContent = {
                    // 스와이프 시 배경 (예: 삭제 아이콘)
                    SwipeDismissBoxBackground(dismissState)
                },
                content = {
                    // 실제 표시될 카드 컨텐츠
                    CourseCard(
                        header = { SoloCourseCardHeader(course) },
                        poseList = poseList,
                        course = course,
                        onClick = onClick,
                        onUpdateCourse = onUpdateCourse
                        // modifier = Modifier.fillMaxWidth() // 카드 너비 채우기 등 필요 시 추가
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDismissBoxBackground(dismissState: SwipeToDismissBoxState) {
    // 스와이프 방향에 따른 배경 색상 및 아이콘 표시
    val color = when {
        dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF6F60) // 빨간색 계열
        else -> Color.Transparent
    }

    val alignment = when {
        dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 16.dp),
        contentAlignment = alignment
    ) {
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = Color.White
            )
        }
    }
}



@Composable
fun PosesRowWithArrows(course: UserCourse) {
    // 외부 Box에 둥근 모서리와 패딩 적용
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // LazyRow에는 별도의 배경을 적용하지 않음 (외부 Box가 배경 역할)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // itemsIndexed를 사용하여 인덱스 기반으로 접근
            itemsIndexed(course.poses) { index, pose ->
                // 포즈 아이템
                PoseItem(pose = pose)

                // 마지막 항목이 아니면 화살표 표시 (인덱스 기반 비교)
                if (index < course.poses.size - 1) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "다음",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


// 각 요가 포즈 아이템
@Composable
fun PoseItem(pose: YogaPose) {
    AsyncImage(
        model = pose.poseImg,
        contentDescription = pose.poseName,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}


@Composable
fun AddCourseButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "코스 추가하기",
                    tint = Color.Black
                )
                Text(
                    text = "코스 추가하기",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }

        }
    }
}


