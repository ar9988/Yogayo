package com.d104.yogaapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MAX_IMAGES_PER_POSE = 10  // 포즈당 최대 이미지 수
        private const val MAX_IMAGE_AGE_DAYS = 14  // 이미지 최대 보존 기간(일)
        private const val MAX_STORAGE_SIZE_BYTES = 50 * 1024 * 1024L  // 최대 50MB
        private const val IMAGE_QUALITY = 80  // JPEG 압축 품질 (0-100)
        private const val MAX_IMAGE_WIDTH = 800  // 최대 이미지 너비
    }

    private val imageDir by lazy {
        File(context.filesDir, "pose_captures").apply {
            if (!exists()) mkdirs()
        }
    }

    // 이미지 저장 함수 - 비트맵을 받아 압축하고 저장
    suspend fun saveImage(bitmap: Bitmap, index:String, poseId: String): Uri? = withContext(Dispatchers.IO) {
        try {
            // 먼저 저장 공간 정리
            cleanupStorage()

            // 이미지 크기 조정
            val resizedBitmap = resizeImage(bitmap)

            // 파일 생성
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "POSE_${index}_${poseId}_${timestamp}.jpg"
            val file = File(imageDir, filename)

            // 비트맵을 파일로 압축 저장
            FileOutputStream(file).use { fos ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fos)
                fos.flush()
            }

            // 원본 비트맵과 다른 경우 리사이즈된 비트맵 리소스 해제
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }

            return@withContext Uri.fromFile(file)
        } catch (e: Exception) {
            Timber.e("이미지 저장 실패: ${e.message}")
            return@withContext null
        }
    }

    // 이미지 크기 조정 함수
    private fun resizeImage(original: Bitmap): Bitmap {
        // 원본 이미지 크기가 이미 작으면 그대로 반환
        if (original.width <= MAX_IMAGE_WIDTH) {
            return original
        }

        // 비율 유지하며 크기 조정
        val ratio = MAX_IMAGE_WIDTH.toFloat() / original.width
        val newWidth = MAX_IMAGE_WIDTH
        val newHeight = (original.height * ratio).toInt()

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    }

    // 저장소 정리 함수
    private suspend fun cleanupStorage() = withContext(Dispatchers.IO) {
        try {
            // 1. 오래된 이미지 정리
            val cutoffDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -MAX_IMAGE_AGE_DAYS)
            }.timeInMillis

            // 2. 포즈별 이미지 수 제한
            val poseImages = imageDir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("POSE_") }
                ?.groupBy { filename ->
                    // 파일명에서 포즈 ID 추출 (POSE_poseId_timestamp.jpg)
                    filename.name.split("_").getOrNull(1) ?: ""
                } ?: emptyMap()

            // 각 포즈별로 초과 이미지 삭제
            poseImages.forEach { (poseId, files) ->
                if (files.size > MAX_IMAGES_PER_POSE) {
                    // 최신순으로 정렬하고 초과분만 삭제
                    files.sortedByDescending { it.lastModified() }
                        .drop(MAX_IMAGES_PER_POSE)
                        .forEach { it.delete() }
                }
            }

            // 3. 총 저장 용량 확인 및 제한
            val totalSize = imageDir.listFiles()
                ?.filter { it.isFile }
                ?.sumOf { it.length() } ?: 0L

            if (totalSize > MAX_STORAGE_SIZE_BYTES) {
                // 용량이 초과되면 가장 오래된 파일부터 삭제
                imageDir.listFiles()
                    ?.filter { it.isFile }
                    ?.sortedBy { it.lastModified() }
                    ?.let { sortedFiles ->
                        var currentSize = totalSize
                        var i = 0

                        // 용량이 제한 이하가 될 때까지 삭제
                        while (currentSize > MAX_STORAGE_SIZE_BYTES && i < sortedFiles.size) {
                            val fileSize = sortedFiles[i].length()
                            sortedFiles[i].delete()
                            currentSize -= fileSize
                            i++
                        }
                    }
            }
        } catch (e: Exception) {
            Timber.e("저장소 정리 실패: ${e.message}")
        }
    }

    // 특정 포즈의 이미지 목록 가져오기
    suspend fun getImagesForPose(poseId: String): List<Uri> = withContext(Dispatchers.IO) {
        try {
            imageDir.listFiles()
                ?.filter {
                    it.isFile && it.name.startsWith("POSE_${poseId}_")
                }
                ?.sortedByDescending { it.lastModified() }
                ?.map { Uri.fromFile(it) }
                ?: emptyList()
        } catch (e: Exception) {
            Timber.e("이미지 목록 조회 실패: ${e.message}")
            emptyList()
        }
    }

    // 이미지 삭제 함수
    suspend fun deleteImage(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val path = uri.path ?: return@withContext false
            val file = File(path)

            if (file.exists() && file.absolutePath.startsWith(imageDir.absolutePath)) {
                file.delete()
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            Timber.e("이미지 삭제 실패: ${e.message}")
            return@withContext false
        }
    }

    // 모든 이미지 삭제 (앱 데이터 초기화 등에 사용)
    suspend fun deleteAllImages(): Boolean = withContext(Dispatchers.IO) {
        try {
            imageDir.listFiles()?.forEach { it.delete() }
            return@withContext true
        } catch (e: Exception) {
            Timber.e("모든 이미지 삭제 실패: ${e.message}")
            return@withContext false
        }
    }

    // 저장소 사용량 확인
    suspend fun getStorageUsage(): Long = withContext(Dispatchers.IO) {
        try {
            imageDir.listFiles()
                ?.filter { it.isFile }
                ?.sumOf { it.length() }
                ?: 0L
        } catch (e: Exception) {
            Timber.e("저장소 사용량 확인 실패: ${e.message}")
            0L
        }
    }
}