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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.yogaapp.features.common.CourseCard
import com.d104.yogaapp.features.common.CustomCourseDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SoloScreen(
    viewModel: SoloViewModel = hiltViewModel(),
    onNavigateToYogaPlay: (UserCourse) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedCourse by remember { mutableStateOf<UserCourse?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 코스 시작 다이얼로그 표시
    selectedCourse?.let { course ->
        YogaCourseStartDialog(
            course = course,
            onDismiss = { selectedCourse = null },
            onConfirm = { updatedCourse ->
                // 튜토리얼 상태가 변경되었으면 코스 업데이트
                if (updatedCourse.tutorial != course.tutorial) {
                    viewModel.handleIntent(
                        SoloIntent.UpdateCourseTutorial(
                            updatedCourse.courseId,
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
                        Image(
                            modifier = Modifier
                                .height(16.dp)
                                .width(24.dp),
                            painter = painterResource(id = R.drawable.ic_yoga),
                            contentDescription = "logo img",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }

                items(state.courses) { course ->
                    if(course.courseId<0){
                        CourseCard(
                            header = { SoloCourseCardHeader(course) },
                            poseList = course.poses,
                            course = course,
                            onClick = { selectedCourse = course }, // 다이얼로그 표시용 코스 선택
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
                        SwipeableCourseDismissBox(
                            course = course,
                            poseList = viewModel.tmpPoseInfo,
                            onClick = { selectedCourse = course }, // 다이얼로그 표시용 코스 선택
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
                            }
                        )
                    }
                }

                if (state.courses.size <= 8) {
                    item {
                        AddCourseButton(
                            poseList = viewModel.tmpPoseInfo,
                            onSaveNewCourse = { courseName, poses ->
                                viewModel.handleIntent(SoloIntent.CreateCourse(courseName, poses))
                                coroutineScope.launch {
                                    delay(100)
                                    listState.animateScrollToItem(state.courses.size)
                                }
                            }
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 코스 이름과 튜토리얼 표시
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // 튜토리얼 여부 표시
                if (course.tutorial == true) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryColor,
                        modifier = Modifier.height(26.dp)
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
            }

            // 예상 시간 표시
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "예상 시간",
                    tint = Color.Gray
                )

                // 각 포즈당 3분으로 계산
                val durationMinutes = course.poses.size * 3
                Text(
                    text = "${durationMinutes}분",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
    // 코스 ID가 0 이상인 경우에만 스와이프 삭제 기능 활성화
    val canDelete = course.courseId >= 0

    // 삭제 상태 관리
    var isRemoved by remember { mutableStateOf(false) }

    // 스와이프 상태 설정
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }, // 40% 이상 스와이프 시 동작
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart && canDelete) {
                isRemoved = true
                true
            } else {
                false
            }
        }
    )

    // 삭제 애니메이션 후 실제 삭제 호출
    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(300) // 애니메이션 시간 동안 대기
            onDeleteCourse(course)
        }
    }
    if(!isRemoved) {
        AnimatedVisibility(
            visible = !isRemoved,
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 300,
                )
            ) + shrinkHorizontally(
                animationSpec = tween(
                    durationMillis = 300,
                ),
                shrinkTowards = Alignment.Start
            )
        ) {
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false, // 오른쪽에서 왼쪽으로 스와이프만 허용
                enableDismissFromEndToStart = canDelete, // ID가 0 이상인 경우만 삭제 활성화
                backgroundContent = {
                    SwipeDismissBoxBackground(dismissState)
                },
                content = {
                    CourseCard(
                        header = {SoloCourseCardHeader(course)},
                        poseList = poseList,
                        course = course,
                        onClick = onClick,
                        onUpdateCourse = onUpdateCourse
                    )
                },
                modifier = Modifier.padding(vertical = 8.dp)
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
    poseList:List<YogaPose>,
    onSaveNewCourse: (String, List<YogaPoseWithOrder>) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    // 대화 상자가 표시되어야 하는 경우
    if (showDialog) {
        CustomCourseDialog(
            poseList = poseList,
            onDismiss = { showDialog = false },
            onSave = { courseName,poses ->
                onSaveNewCourse(courseName,poses)
                showDialog = false
            }
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
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


