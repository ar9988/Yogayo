package com.d104.yogaapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64 // [!code ++]
import java.io.ByteArrayOutputStream

fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        // 1. Base64 문자열 정제 (접두사 제거)
        val pureBase64 = base64String.split(",").lastOrNull() ?: base64String

        // 2. Base64 → ByteArray 디코딩 (올바른 플래그 사용)
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT) // [!code ++]

        // 3. Bitmap 생성
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun bitmapToBase64(bitmap: Bitmap): String? {
    return try {
        // 1. ByteArrayOutputStream 생성
        val outputStream = ByteArrayOutputStream()

        // 2. Bitmap 압축 (JPEG 형식, 100% 품질)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        // 3. ByteArray로 변환
        val byteArray = outputStream.toByteArray()

        // 4. Base64 인코딩
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}