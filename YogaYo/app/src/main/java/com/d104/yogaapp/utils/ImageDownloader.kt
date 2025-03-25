package com.d104.yogaapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
    @ApplicationContext private val context: Context
) {
    suspend fun saveImageToGallery(
        imageUri: Uri,
        poseName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 입력 스트림 열기
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // 타임스탬프로 파일명 생성
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "YogaYo_${poseName.replace(" ", "_")}_$timestamp.jpg"

            var outputStream: OutputStream? = null
            var imageUriResult: Uri? = null
            var success = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 이상에서는 MediaStore API 사용
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/YogaYo")
                }

                context.contentResolver.also { resolver ->
                    imageUriResult = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    outputStream = imageUriResult?.let { resolver.openOutputStream(it) }
                }
            } else {
                // Android 9 이하에서는 직접 파일 생성
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/YogaYo"
                val dir = File(imagesDir)
                if (!dir.exists()) dir.mkdirs()
                val image = File(imagesDir, fileName)
                outputStream = FileOutputStream(image)
                imageUriResult = Uri.fromFile(image)
            }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                success = true
            }

            // 갤러리 스캔 요청
            if (success && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                imageUriResult?.let {
                    context.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, it))
                }
            }

            // 메인 스레드에서 토스트 표시
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "이미지가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            return@withContext success
        } catch (e: Exception) {
            Timber.e(e, "이미지 저장 실패")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "이미지 저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
            return@withContext false
        }
    }
}