package com.d104.yogaapp.features.multi.play.components

import androidx.compose.foundation.Image // 사용하지 않으므로 제거 가능 (Coil 사용 시)
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // 로딩 인디케이터 추가
import androidx.compose.material3.MaterialTheme // MaterialTheme 사용 (선택적)
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Context 가져오기 위해 추가
import androidx.compose.ui.res.painterResource // 플레이스홀더/에러 리소스 위해 유지
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Coil 임포트
import coil.compose.SubcomposeAsyncImage // 로딩/에러 상태 처리를 위해 SubcomposeAsyncImage 사용
import coil.request.ImageRequest // Coil 요청 생성 위해 임포트
import com.d104.yogaapp.R // 리소스 ID 사용 위해 유지

@Composable
fun GalleryPhotoCard(
    url: String,
    name: String,
    onClick: () -> Unit
) {
    // Box 제거 가능: Card가 최상위 요소가 되어도 무방
    // Box(
    //     modifier = Modifier.fillMaxSize(), // fillMaxSize는 Grid 셀 내에서 문제를 일으킬 수 있음
    //     contentAlignment = Alignment.Center
    // ) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(180.dp) // 너비 고정 (Grid 셀 크기에 따라 조정 필요)
            .clickable { onClick() }, // 클릭 리스너
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // --- Coil 이미지 로딩 ---
            SubcomposeAsyncImage( // 로딩/에러 상태 직접 처리 가능
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url) // 이미지 URL 설정
                    .crossfade(true) // 부드러운 전환 효과
                    // .diskCacheKey(url) // 캐시 키 명시 (선택적)
                    // .memoryCacheKey(url) // 메모리 캐시 키 명시 (선택적)
                    .build(),
                contentDescription = name, // 콘텐츠 설명 (접근성)
                modifier = Modifier
                    .fillMaxWidth()
                    // .height(160.dp) // 고정 높이 대신 비율 사용 권장
                    .aspectRatio(1f) // 1:1 비율 유지 (정사각형) 또는 원하는 비율
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)), // 위쪽 모서리 둥글게
                contentScale = ContentScale.Crop, // 이미지 잘라서 채우기
                loading = { // 로딩 중 표시할 컴포저블
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                },
                error = { // 에러 발생 시 표시할 컴포저블
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // 에러 이미지 리소스 (준비 필요)
                            contentDescription = "Error loading image"
                        )
                    }
                }
                // success = { painter -> // 성공 시 특별한 처리 필요하면 사용 (기본은 이미지 표시)
                //     Image(painter = painter.painter, ...)
                // }
            )
            // --------------------------

            // Text Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start, // 텍스트를 왼쪽에 정렬

            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
    // } // Box 제거됨
}