package com.d104.yogaapp.features.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.d104.yogaapp.R

@Composable
fun LottieImage(
    url: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Url(url)
    )

    // Lottie 애니메이션의 진행 상태
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = false,
        iterations = LottieConstants.IterateForever // 무한 반복
    )

    // 로딩 상태 추적
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // 로딩 상태 업데이트
    LaunchedEffect(composition) {
        isLoading = composition == null
        hasError = composition == null && !isLoading
    }

    Box(modifier = modifier) {
        // 메인 Lottie 애니메이션
        if (!hasError && composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 로딩 중 표시
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 에러 표시
        if (hasError) {
            Image(
                painter = painterResource(id = R.drawable.img_sample_pose),
                contentDescription = "Error loading animation",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}