package com.d104.yogaapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import timber.log.Timber // Timber 로깅 추가

fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        // 1. Base64 문자열 정제 (접두사 제거)
        val pureBase64 = base64String.split(",").lastOrNull() ?: base64String

        // 2. Base64 → ByteArray 디코딩 (올바른 플래그 사용)
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)

        // 3. Bitmap 생성
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        Timber.e(e, "Failed to decode Base64 string to Bitmap.") // 에러 로그 추가
        null
    } catch (e: OutOfMemoryError) {
        Timber.e(e, "OutOfMemoryError while decoding Base64 to Bitmap.") // 메모리 부족 에러 처리
        null
    } catch (e: Exception) {
        Timber.e(e, "Unexpected error decoding Base64 to Bitmap.") // 기타 에러 처리
        null
    }
}

fun bitmapToBase64(bitmap: Bitmap?): ByteArray? { // 입력 Bitmap을 Nullable로 받는 것이 더 안전할 수 있음
    if (bitmap == null) {
        Timber.w("bitmapToBase64 called with null bitmap.")
        return null
    }
    // 추가: 유효하지 않은 비트맵(recycle 등) 체크
    if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
        Timber.w("bitmapToBase64 called with invalid bitmap (recycled or zero size).")
        return null
    }

    var outputStream: ByteArrayOutputStream? = null // finally 에서 닫기 위해 바깥에 선언
    return try {
        // 1. ByteArrayOutputStream 생성
        outputStream = ByteArrayOutputStream()

        // *** 2. 비트맵 데이터를 스트림에 압축하여 쓰기 (핵심!) ***
        //    - format: 압축 형식 (JPEG, PNG, WEBP 등)
        //    - quality: 압축 품질 (0-100, JPEG/WEBP만 해당)
        //    - stream: 출력 스트림
        val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream) // 예시: JPEG, 품질 85

        if (!success) {
            Timber.e("Bitmap.compress failed!")
            return null // 압축 실패 시 null 반환
        }

        // 3. ByteArray로 변환
        val byteArray = outputStream.toByteArray()
        Timber.d("bitmapToBase64 successful. Bitmap: ${bitmap.width}x${bitmap.height}, Output size: ${byteArray.size}")
        byteArray // 성공 시 바이트 배열 반환

    } catch (e: Exception) {
        Timber.e(e, "Error during bitmapToBase64 conversion.")
        null // 오류 발생 시 null 반환
    } finally {
        // 스트림 닫기 (리소스 누수 방지)
        try {
            outputStream?.close()
        } catch (e: Exception) {
            Timber.e(e, "Error closing ByteArrayOutputStream.")
        }
    }
}