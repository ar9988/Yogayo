package com.d104.yogaapp.features.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.Matrix


fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun RotateScreen(context: Context) {
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // 상단바와 네비게이션 바 숨기기
        WindowCompat.setDecorFitsSystemWindows(activity?.window ?: return@DisposableEffect onDispose {}, false)

        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)

            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            // 추가: 전체 화면 플래그 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        onDispose {
            val act = context.findActivity()
            act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // 시스템 바 및 화면 모드 복원
            act?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, true)

                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.statusBars())
                    show(WindowInsetsCompat.Type.navigationBars())
                }

                // 플래그 제거
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }
        }
    }
}

@Composable
fun ShowToast(message: String) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

fun resizeBitmapWithMatrix(originalBitmap: Bitmap, targetWidth: Int = 224, targetHeight: Int = 224): Bitmap {
    val width = originalBitmap.width
    val height = originalBitmap.height

    val scaleWidth = targetWidth.toFloat() / width
    val scaleHeight = targetHeight.toFloat() / height

    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)

    return Bitmap.createBitmap(
        originalBitmap,
        0,
        0,
        width,
        height,
        matrix,
        true
    )
}