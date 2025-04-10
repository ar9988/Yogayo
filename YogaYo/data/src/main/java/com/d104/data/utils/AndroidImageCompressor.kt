package com.d104.data.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.d104.domain.utils.ImageCompressor
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class AndroidImageCompressor @Inject constructor() : ImageCompressor {
    override fun compress(input: ByteArray, quality: Int): ByteArray {
        // 이전 compressImage 함수의 로직 사용
        var bitmap: Bitmap? = null
        var outputStream: ByteArrayOutputStream? = null
        try {
            bitmap = BitmapFactory.decodeByteArray(input, 0, input.size)
                ?: throw IllegalArgumentException("Could not decode image byte array for compression")
            outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            return outputStream.toByteArray()
        } finally {
            bitmap?.recycle()
            outputStream?.close() // close() 는 try-with-resources 나 finally 에서 호출 권장
        }
    }
}