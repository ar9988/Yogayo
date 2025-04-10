package com.d104.yogaapp.features.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.domain.model.YogaPose
import com.d104.yogaapp.ui.theme.PastelGreen
import com.d104.yogaapp.ui.theme.PastelRed

@Composable
fun YogaAnimationScreen(
    pose:YogaPose,
    accuracy:Float,
    isPlaying:Boolean
){
    val threshholds = 70f
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            // accuracy에 따라 다른 피드백 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        // 70 이상이면 녹색, 미만이면 빨간색
                        color = if (accuracy >= threshholds) PastelGreen else PastelRed
                    ),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = if (accuracy >= threshholds) "훌륭해요!" else "자세가 불안정해요!",
//                    text = "정확도: ${String.format("%.1f%%", accuracy)}",
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Lottie 표시 - isPlaying 상태 전달
                GifImage(
                    url = pose.poseAnimation,
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = isPlaying,
                    poseId = pose.poseId
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 포즈 이름
            Text(
                text = pose.poseName,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }



    }
}