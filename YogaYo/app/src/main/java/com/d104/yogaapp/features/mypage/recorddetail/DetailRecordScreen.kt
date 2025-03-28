package com.d104.yogaapp.features.mypage.recorddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.UserRecord
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.UserRecordCard
import kotlin.math.ceil

@Composable
fun DetailRecordScreen(
    userRecord: UserRecord,
    viewModel: DetailRecordViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 스크롤 가능한 콘텐츠 영역
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 상단 카드 (UserRecordCard 사용)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                UserRecordCard(
                    userRecord = userRecord,
                    showDetailButton = false
                )
                Spacer(modifier = Modifier.height(24.dp))
            }


            val poses = state.bestPoseRecords
            val itemsPerRow = 2

            // 각 행을 개별 item으로 추가
            for (i in poses.indices step itemsPerRow) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (j in 0 until itemsPerRow) {
                            val index = i + j
                            if (index < poses.size) {
                                BestPoseRecordItem(
                                    bestPoseHistory = poses[index],
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // 빈 공간
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 추가 여백
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}



@Composable
fun BestPoseRecordItem(
    bestPoseHistory: BestPoseRecord,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .height(160.dp), // 고정 높이 설정
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 요가 포즈 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(bestPoseHistory.poseImg)
                        .crossfade(true)
                        .error(R.drawable.ic_yoga)
                        .build(),
                    contentDescription = bestPoseHistory.poseName,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 포즈 이름

            Text(
                text = bestPoseHistory.poseName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 정확도
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "나의 정확도",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.2f%%", bestPoseHistory.bestAccuracy),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}