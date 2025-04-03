package com.d104.yogaapp.features.multi.play.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.d104.yogaapp.R
import com.d104.yogaapp.features.solo.play.PauseActionButton


@Composable
fun MenuOverlay(
    onResume: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 계속하기 버튼
            PauseActionButton(
                icon = R.drawable.ic_resume,
                text = "계속하기",
                onClick = onResume
            )
            // 나가기 버튼
            PauseActionButton(
                icon = R.drawable.ic_exit,
                text = "나가기",
                onClick = onExit
            )
        }
    }
}
