package com.d104.yogaapp.features.solo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.yogaapp.features.common.GifImage
import com.d104.yogaapp.features.common.PermissionChecker
import com.d104.yogaapp.features.common.YogaPlayScreen
import com.d104.yogaapp.features.common.findActivity
import com.d104.yogaapp.features.solo.play.SoloYogaPlayIntent
import com.d104.yogaapp.features.solo.play.SoloYogaPlayViewModel


@Composable
fun SoloScreen(onNavigateToYogaPlay: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onNavigateToYogaPlay) {
                Text("요가 카메라")
            }
        }
    }

}


//// 권한 다시 요청을 위한 확장 기능
//@Composable
//fun RequestPermissionLauncher(
//    onResult: (Boolean) -> Unit
//): ManagedActivityResultLauncher<String, Boolean> {
//    return rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//        onResult = onResult
//    )
//}
