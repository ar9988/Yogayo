package com.d104.yogaapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader // Coil ImageLoader 주입
) {

    /**
     * 주어진 이미지 소스(URL 문자열 또는 Content URI 문자열)로부터 이미지를 가져와
     * 기기의 갤러리 (Pictures/YogaYo 폴더)에 저장합니다.
     *
     * @param imageSource 다운로드할 이미지의 URL 문자열 또는 앱 내부 저장소의 Content URI 문자열.
     * @param poseName 파일명에 사용될 포즈 이름.
     * @return 저장 성공 여부.
     *
     * 필수 권한:
     * - INTERNET (URL 다운로드 시)
     * - WRITE_EXTERNAL_STORAGE (Android 9 이하에서 외부 저장소 접근 시)
     */
    suspend fun saveImageToGallery(
        imageSource: String, // URL 또는 Content URI 문자열
        poseName: String
    ): Boolean = withContext(Dispatchers.IO) { // IO 작업을 위한 코루틴 컨텍스트
        try {
            val bitmap: Bitmap? = getBitmapFromSource(imageSource)

            if (bitmap == null) {
                Timber.e("이미지 소스로부터 비트맵을 가져오지 못했습니다: $imageSource")
                showToast("이미지를 불러올 수 없습니다.")
                return@withContext false
            }

            // 파일명 생성 (기존 로직 유지)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "YogaYo_${poseName.replace(" ", "_")}_$timestamp.jpg"

            var outputStream: OutputStream? = null
            var imageUriResult: Uri? = null
            var success = false

            // MediaStore API 또는 직접 파일 생성 로직 (기존 로직 유지)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/YogaYo")
                    put(MediaStore.MediaColumns.IS_PENDING, 1) // 파일을 쓰는 동안 PENDING 상태로 설정
                }

                context.contentResolver.also { resolver ->
                    imageUriResult = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    outputStream = imageUriResult?.let { resolver.openOutputStream(it) }

                    // 파일 쓰기 완료 후 PENDING 상태 해제
                    outputStream?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        imageUriResult?.let { resolver.update(it, contentValues, null, null) }
                        success = true
                    } ?: run {
                        // 스트림 열기 실패 시 삽입된 URI 삭제
                        imageUriResult?.let { resolver.delete(it, null, null) }
                        success = false
                    }
                }
            } else {
                // Android 9 이하 (권한 필요)
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/YogaYo"
                val dir = File(imagesDir)
                if (!dir.exists()) dir.mkdirs()
                val imageFile = File(imagesDir, fileName)
                try {
                    outputStream = FileOutputStream(imageFile)
                    outputStream?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        success = true
                    }
                    // 미디어 스캐너에 파일 추가 요청
                    if (success) {
                        imageUriResult = Uri.fromFile(imageFile)
                        imageUriResult?.let {
                            context.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, it))
                        }
                    }
                } catch (ioe: Exception) {
                    Timber.e(ioe, "파일 쓰기 실패 (Android 9 이하)")
                    success = false
                } finally {
                    outputStream?.close() // 명시적 close (use 블록이 있지만 안전하게)
                }
            }


            // 결과 토스트 메시지 (메인 스레드에서)
            showToast(if (success) "이미지가 갤러리에 저장되었습니다." else "이미지 저장에 실패했습니다.")

            return@withContext success

        } catch (e: Exception) {
            Timber.e(e, "이미지 저장 중 오류 발생")
            showToast("이미지 저장 중 오류가 발생했습니다: ${e.localizedMessage}")
            return@withContext false
        }
    }

    // --- 이미지 소스(URL 또는 URI)로부터 Bitmap을 가져오는 내부 함수 ---
    private suspend fun getBitmapFromSource(source: String): Bitmap? {
        return try {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                // URL인 경우 Coil 사용
                val request = ImageRequest.Builder(context)
                    .data(source)
                    .allowHardware(false) // 저장에는 Software Bitmap이 더 적합할 수 있음
                    .build()
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    // Drawable을 Bitmap으로 변환
                    result.drawable.toBitmap()
                } else {
                    Timber.e("Coil 이미지 로드 실패: $source, 결과: $result")
                    null
                }
            } else {
                // Content URI인 경우 기존 방식 사용
                val imageUri = Uri.parse(source) // 문자열을 Uri로 변환
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Bitmap 로드 실패: $source")
            null
        }
    }

    // --- 메인 스레드에서 토스트 메시지를 표시하는 헬퍼 함수 ---
    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}