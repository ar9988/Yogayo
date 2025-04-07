package com.d104.yogaapp.features.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseInCourse
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.yogaapp.R
import com.d104.yogaapp.features.solo.PosesRowWithArrows
import com.d104.yogaapp.ui.theme.GrayCardColor
import com.d104.yogaapp.ui.theme.Neutral70

@Composable
fun CourseCard(
    header: @Composable () -> Unit,
    poseList: List<YogaPose> = emptyList(),
    course: UserCourse,
    onClick: () -> Unit,
    onUpdateCourse: (String, List<YogaPoseWithOrder>) -> Unit = { _, _ -> },
    showEditButton: Boolean = true
) {

    var showDialog by remember { mutableStateOf(false) }

    // 대화 상자가 표시되어야 하는 경우
    if (showDialog) {
        CustomCourseDialog(
            originalCourseName = course.courseName,
            poseInCourse = course.poses.mapIndexed{index, yogaPose->
                YogaPoseInCourse(
                    uniqueID ="${course.courseId}-${yogaPose.poseId}-${index}",
                    pose = yogaPose)
            },
            poseList = poseList,
            onDismiss = { showDialog = false },
            onSave = { courseName,poses ->
                onUpdateCourse(courseName,poses)
                showDialog = false
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
                IconButton(
                    onClick = {showDialog = true},
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "수정버튼",
                        tint = Neutral70,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            header()

            Spacer(modifier = Modifier.height(6.dp))

            // 포즈 이미지 행
            PosesRowWithArrows(course = course)
        }
    }
}