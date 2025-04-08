package com.d104.yogaapp.features.multi

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.domain.model.Room
import com.d104.yogaapp.features.common.CourseCard
import androidx.compose.ui.platform.LocalDensity
import com.d104.yogaapp.features.common.CustomCourseDialog
import com.d104.yogaapp.features.multi.dialog.CreateRoomDialog
import com.d104.yogaapp.features.multi.dialog.EnterRoomDialog
import timber.log.Timber

@Composable
fun MultiScreen(
    onNavigateMultiPlay: (Room) -> Unit,
    viewModel: MultiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // 토스트 메시지를 표시한 후 errorMessage를 null로 재설정
            viewModel.processIntent(MultiIntent.ClearErrorMessage)
        }
    }
    LaunchedEffect(uiState.enteringRoom) {
        if (uiState.enteringRoom) {
            viewModel.processIntent(MultiIntent.EnterRoomComplete)
            onNavigateMultiPlay(
                uiState.selectedRoom!!
            )
            Timber.tag("MultiScreen")
                .d("Navigating to MultiPlayScreen with room: ${uiState.enteringRoom}")
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = uiState.roomSearchText,
                onValueChange = { viewModel.processIntent(MultiIntent.UpdateSearchText(it)) },
                label = { Text("검색") },
                placeholder = { Text("검색어를 입력하세요") },
                trailingIcon = {
                    IconButton(onClick = { viewModel.processIntent(MultiIntent.SearchRoom) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Button"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.processIntent(MultiIntent.SearchRoom) }
                ),
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF7F6FA),
                    focusedContainerColor = Color(0xFFF7F6FA)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            RoomList(
                state = scrollState,
                rooms = uiState.page,
                onItemClick = { room ->
                    viewModel.processIntent(MultiIntent.SelectRoom(room))
                },
                onOverScrollTop = { viewModel.processIntent(MultiIntent.PrevPage) },
                onOverScrollBottom = { viewModel.processIntent(MultiIntent.NextPage) }
            )
        }

        FloatingActionButton(
            onClick = {
                // 방 생성 다이얼로그 표시
                viewModel.processIntent(MultiIntent.ClickCreateRoomButton)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "방 만들기"
            )
        }
    }
    // Dialog
    // 방 생성 다이얼로그
    CreateRoomDialog(
        showDialog = uiState.dialogState == DialogState.CREATING,
        roomTitle = uiState.roomTitle,
        roomPassword = uiState.roomPassword,
        onRoomTitleChange = { viewModel.processIntent(MultiIntent.UpdateRoomTitle(it)) },
        onRoomPasswordChange = { viewModel.processIntent(MultiIntent.UpdateRoomPassword(it)) },
        onRoomPasswordChecked = { viewModel.processIntent(MultiIntent.UpdateRoomPasswordChecked(it)) },
        onConfirm = { viewModel.processIntent(MultiIntent.CreateRoom) },
        onDismiss = { viewModel.processIntent(MultiIntent.DismissDialog(DialogState.CREATING)) },
        onCourseSelect = { viewModel.processIntent(MultiIntent.SelectCourse(it)) },
        onEditCourse = { viewModel.processIntent(MultiIntent.ShowEditDialog) },
        userCourses = uiState.yogaCourses,
        onMaxCountChanged = {viewModel.processIntent(MultiIntent.UpdateRoomMaxCount(it))}
    )
    // 방 참가 다이얼로그
    EnterRoomDialog(
        selectedRoom = uiState.selectedRoom,
        showDialog = uiState.dialogState == DialogState.ENTERING,
        onConfirm = { viewModel.processIntent(MultiIntent.EnterRoom) },
        onDismiss = { viewModel.processIntent(MultiIntent.DismissDialog(DialogState.ENTERING)) },
        roomPassword = uiState.roomPassword,
        onRoomPasswordChange = { viewModel.processIntent(MultiIntent.UpdateRoomPassword(it)) }
    )
    // 코스 수정 다이얼로그
    if (uiState.dialogState == DialogState.COURSE_EDITING) {
        CustomCourseDialog(
            poseList = uiState.selectedCourse!!.poses,
            onDismiss = { viewModel.processIntent(MultiIntent.DismissDialog(DialogState.COURSE_EDITING)) },
            onSave = { courseName, poses ->
                viewModel.processIntent(
                    MultiIntent.EditCourse(
                        uiState.selectedCourse!!.courseId,
                        courseName,
                        poses
                    )
                )
            }
        )
        println(uiState.selectedCourse.toString())
    }
}

@Composable
fun RoomList(
    state: LazyListState,
    rooms: List<Room>,
    onItemClick: (Room) -> Unit,
    onOverScrollTop: () -> Unit,
    onOverScrollBottom: () -> Unit
) {
    val density = LocalDensity.current
    val overScrollState = remember { mutableStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (state.firstVisibleItemIndex == 0 && available.y > 0) {
                    overScrollState.value = available.y
                    onOverScrollTop()
                } else if (state.layoutInfo.visibleItemsInfo.lastOrNull()?.index == rooms.size - 1 && available.y < 0) {
                    overScrollState.value = available.y
                    onOverScrollBottom()
                }
                return Offset.Zero
            }
        }
    }
    LazyColumn(
        state = state,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rooms) { room ->
            val onClickRemembered = remember(room) {
                { onItemClick(room) }
            }
            CourseCard(
                header = {
                    MultiCourseCardHeader(
                        room
                    )
                },
                poseList = room.userCourse.poses,
                course = room.userCourse,
                onClick = onClickRemembered,
                showEditButton = false
            )
        }
    }
}

@Composable
fun MultiCourseCardHeader(room: Room) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.5f)
                ) {
                    if (room.hasPassword) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "비밀번호 필요함",
                            modifier = Modifier.rotate(90f)
                        )
                    }
                    Text(
                        text = room.roomName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "현재 인원 수"
                    )
                    Text(
                        text = "${room.roomCount}/${room.roomMax}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(0.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "방장"
                    )
                    Text(
                        text = room.userNickname,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                val durationMinutes = room.userCourse.poses.size * 3
                Text(
                    text = "${durationMinutes}분",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}