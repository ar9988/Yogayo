package com.d104.yogaapp.features.multi.play.components


import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.d104.domain.model.MultiPhoto
import com.d104.yogaapp.R
import com.d104.yogaapp.ui.theme.Neutral20

@Composable
fun DetailPhotoCard(
    resultData: MultiPhoto,
    onDownload: (Uri, String) -> Unit,
    myName: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(8.dp),
                        spotColor = Color.Gray
                    )
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.LightGray
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    when (resultData.ranking) {
                        1 -> { // 1등
                            Image(
                                painter = painterResource(id = R.drawable.ic_gold_medal),
                                contentDescription = "금메달", // 접근성을 위한 설명
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp) // 아이콘과 텍스트 사이에 약간의 간격 추가
                            )
                        }

                        2 -> { // 2등
                            Image(
                                painter = painterResource(id = R.drawable.ic_silver_medal),
                                contentDescription = "은메달",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        3 -> { // 3등
                            Image(
                                painter = painterResource(id = R.drawable.ic_bronze_medal),
                                contentDescription = "동메달",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        // 4등 이상은 아무것도 표시하지 않음 (else 블록 필요 없음)
                    }

                    // 사용자 이름 표시
                    Text(resultData.userName)
                    // 이미지
                    SubcomposeAsyncImage( // 로딩/에러 상태 직접 처리 가능
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(resultData.poseUrl) // 이미지 URL 설정
                            .crossfade(true) // 부드러운 전환 효과
                            // .diskCacheKey(url) // 캐시 키 명시 (선택적)
                            // .memoryCacheKey(url) // 메모리 캐시 키 명시 (선택적)
                            .build(),
                        contentDescription = "동작 이미지", // 콘텐츠 설명 (접근성)
                        modifier = Modifier
                            .fillMaxWidth()
                            // .height(160.dp) // 고정 높이 대신 비율 사용 권장
                            .aspectRatio(1f) // 1:1 비율 유지 (정사각형) 또는 원하는 비율
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp
                                )
                            ), // 위쪽 모서리 둥글게
                        contentScale = ContentScale.Crop, // 이미지 잘라서 채우기
                        loading = { // 로딩 중 표시할 컴포저블
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        error = { // 에러 발생 시 표시할 컴포저블
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // 에러 이미지 리소스 (준비 필요)
                                    contentDescription = "Error loading image"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 텍스트
                    Text(
                        text = "동작 유지시간 : ${resultData.poseTime} 초",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "최고 일치율 : ${resultData.accuracy} %",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
    //////////////////////////////

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Neutral20
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (resultData.ranking) {
                        1 -> { // 1등
                            Image(
                                painter = painterResource(id = R.drawable.ic_gold_medal),
                                contentDescription = "금메달", // 접근성을 위한 설명
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp) // 아이콘과 텍스트 사이에 약간의 간격 추가
                            )
                        }

                        2 -> { // 2등
                            Image(
                                painter = painterResource(id = R.drawable.ic_silver_medal),
                                contentDescription = "은메달",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        3 -> { // 3등
                            Image(
                                painter = painterResource(id = R.drawable.ic_bronze_medal),
                                contentDescription = "동메달",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        4 -> { // 4등
                            Image(
                                painter = painterResource(id = R.drawable.ic_yoga),
                                contentDescription = "등외",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                    }
                    // 포즈 이름
                    Text(
                        text = resultData.userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 다운로드 버튼
                if (resultData.userName==myName) {
                    IconButton(
                        onClick = {
                            onDownload(Uri.parse(resultData.poseUrl),resultData.userName)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "이미지 다운로드",
                            tint = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(resultData.poseUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = resultData.userName,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 결과 정보
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "동작 유지시간: ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.2f초", resultData.poseTime),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "최고 일치율: ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(resultData.accuracy).toInt()}.${((resultData.accuracy) % 1 * 10).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_best),
                        contentDescription = "최고 일치율",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
            }

        }
    }
}