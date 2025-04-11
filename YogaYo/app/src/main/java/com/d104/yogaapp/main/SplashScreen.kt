package com.d104.yogaapp.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight // 기본 FontWeight 사용 시
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
// import com.yourpackage.ui.theme.SplashFontFamily // 커스텀 폰트 사용 시 import
import com.d104.yogaapp.R
import com.d104.yogaapp.ui.theme.PrimaryColor
import com.d104.yogaapp.ui.theme.SplashFontFamily

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val view = LocalView.current // 현재 Composable의 View 가져오기
    val window = (view.context as? android.app.Activity)?.window // View의 Context에서 Window 가져오기

    // DisposableEffect를 사용하여 스플래시 화면이 보일 때/사라질 때 시스템 UI 제어
    DisposableEffect(Unit) { // key가 Unit이면 컴포저블이 처음 나타날 때 실행
        if (window != null) {
            val originalSystemUiVisibility = window.decorView.systemUiVisibility // 이전 상태 저장 (필요 시)
            val insetsController = WindowCompat.getInsetsController(window, view)

            // 1. 상태 표시줄과 네비게이션 바 숨기기
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 2. 전체 화면 사용 설정 (Edge-to-Edge) - 이미 Activity에서 설정했다면 중복 X
             WindowCompat.setDecorFitsSystemWindows(window, false) // 스플래시만 할 경우 여기서 제어 가능

            // 3. 숨겨진 바가 다시 나타나는 방식 설정 (스와이프로 임시 표시)
//            insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // onDispose: 컴포저블이 사라질 때 실행될 클린업 로직
            onDispose {
                // 1. 숨겼던 시스템 바 다시 표시
                insetsController?.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)

                // 2. 전체 화면 사용 설정 원복 (만약 여기서 설정했다면)
                // WindowCompat.setDecorFitsSystemWindows(window, true)

                // 시스템 바 동작 기본값 복구 (필요 시 - 보통 show만 해도 괜찮음)
                // window.decorView.systemUiVisibility = originalSystemUiVisibility // 이전 방식으로 복구 시
            }
        } else {
            // Window 객체를 가져올 수 없는 경우의 처리 (일반적으로 발생하지 않음)
            onDispose { } // 비워둠
        }
    }

    // val backgroundColor = PrimaryColor // 정의된 색상 사용
    val backgroundColor = PrimaryColor // 직접 지정 예시

    Box( // 메인 컨테이너 역할을 Box가 수행
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 1. 텍스트 요소들을 담는 Column (Box 내부에 배치)
        Column(
            modifier = Modifier
                // Box의 상단 중앙에 정렬
                .align(Alignment.TopCenter)
                // 상단에서 적절한 패딩을 주어 위치 조절 (값 조정 필요)
                .padding(top = 150.dp), // 화면 상단으로부터의 거리
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "요가요" 텍스트
            Text(
                text = "요가요",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                 fontFamily = SplashFontFamily // 커스텀 폰트
            )

            // "Red Law" 텍스트
            Text(
                text = "Red Law",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = SplashFontFamily, // 커스텀 폰트
                fontWeight = FontWeight.Bold // 또는 Normal
            )
        }

        // 2. 고양이 이미지 (Box 내부에 배치)
        Image(
            painter = painterResource(id = R.drawable.img_splash_cat), // 리소스 ID 확인
            contentDescription = "Yoga Cat Splash Image",
            modifier = Modifier
                // Box의 하단 중앙에 정렬
                .align(Alignment.BottomCenter)
                // 가로 너비를 꽉 채움
                .fillMaxWidth(),
            // 이미지가 너비에 맞춰지도록 ContentScale 조정
            // Fit: 전체 이미지가 보이도록 하되, 비율 유지를 위해 위아래 여백 생길 수 있음
            // FillWidth: 너비를 꽉 채우고 비율 유지. 세로가 잘릴 수 있음
            // FillBounds: 비율 무시하고 꽉 채움 (이미지 왜곡됨)
            // Crop: 비율 유지하고 꽉 채우되, 남는 부분 잘라냄
            contentScale = ContentScale.FillWidth // 또는 ContentScale.Fit 시도 후 결정
        )
    }
}
