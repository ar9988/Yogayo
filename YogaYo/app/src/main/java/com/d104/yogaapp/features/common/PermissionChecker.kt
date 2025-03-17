package com.d104.yogaapp.features.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object PermissionChecker {

    /**
     * 지정된 권한의 상태를 확인하고 요청하는 Composable 함수
     *
     * @param permission 요청할 권한 (예: Manifest.permission.CAMERA)
     * @param onPermissionResult 권한 상태가 변경될 때 호출되는 콜백
     */
    @Composable
    fun CheckPermission(
        permission: String,
        onPermissionResult: (Boolean) -> Unit
    ) {
        val context = LocalContext.current
        var permissionGranted by remember { mutableStateOf(false) }

        // 권한 요청 런처
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionGranted = isGranted
            onPermissionResult(isGranted)
        }

        // 초기 권한 상태 확인
        LaunchedEffect(Unit) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    permissionGranted = true
                    onPermissionResult(true)
                }
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(permission)
                }
            }
        }
    }

    /**
     * 권한 상태를 확인하는 유틸리티 함수
     *
     * @param context 컨텍스트
     * @param permission 확인할 권한
     * @return 권한이 부여되었는지 여부
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 카메라 권한 상태를 확인하는 편의 함수
     *
     * @param context 컨텍스트
     * @return 카메라 권한이 부여되었는지 여부
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.CAMERA)
    }
}