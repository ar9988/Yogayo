package com.d104.yogaapp.features.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseInCourse
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.yogaapp.features.solo.PosesRowWithArrows
import com.d104.yogaapp.ui.theme.GrayCardColor

@Composable
fun CourseCard(
    content: @Composable () -> Unit,
    poseList: List<YogaPose> = emptyList(),
    course: UserCourse,
    onClick: () -> Unit,
    onUpdateCourse: (String, List<YogaPoseWithOrder>) -> Unit = { _, _ -> },
    onDeleteCourse:(UserCourse)->Unit = {},
    showEditButton: Boolean = true
) {

    var showEditDialog by remember(course.courseId) { mutableStateOf(false) }
    var showDeleteDialog by remember(course.courseId) { mutableStateOf(false) }

    // 대화 상자가 표시되어야 하는 경우
    if (showEditDialog) {
        CustomCourseDialog(
            originalCourseName = course.courseName,
            poseInCourse = course.poses.mapIndexed{index, yogaPose->
                YogaPoseInCourse(
                    uniqueID ="${course.courseId}-${yogaPose.poseId}-${index}",
                    pose = yogaPose)
            },
            poseList = poseList,
            onDismiss = { showEditDialog = false },
            onSave = { courseName,poses ->
                onUpdateCourse(courseName,poses)
                showEditDialog = false
            }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("코스 삭제") },
            text = { Text("${course.courseName} 코스를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteCourse(course)
                    },
                    // --- 색상 커스터마이징 ---
                    colors = ButtonDefaults.textButtonColors(
                        // 배경색: 테마의 에러 색상 (일반적으로 빨간색 계열)
                        containerColor = MaterialTheme.colorScheme.error,
                        // 내용(텍스트) 색상: 에러 배경 위에 표시될 색상 (일반적으로 흰색)
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                    // --- ---
                ) {
                    // Text의 color는 ButtonDefaults에서 설정한 contentColor를 따름
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                    // 취소 버튼은 기본 스타일 유지 (또는 필요시 커스터마이징)
                ) {
                    Text("취소")
                }
            }
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GrayCardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 상단 헤더: 코스 이름, 튜토리얼 여부, 예상 시간
            if(showEditButton&&course.courseId>=0){
                Row(
                    modifier = Modifier.fillMaxWidth(), // Row가 가로 전체 너비를 차지하도록 함
                    horizontalArrangement = Arrangement.End, // 내부 요소들을 오른쪽 끝으로 정렬
                    verticalAlignment = Alignment.CenterVertically // 아이콘들을 수직 중앙 정렬 (선택 사항)
                ) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제")
                    }
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(32.dp) // 크기 지정은 개별 아이콘에
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "수정")
                    }


                    // 다른 IconButton 추가 가능

                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            content()

            Spacer(modifier = Modifier.height(6.dp))

            // 포즈 이미지 행
            PosesRowWithArrows(course = course)
        }
    }
}