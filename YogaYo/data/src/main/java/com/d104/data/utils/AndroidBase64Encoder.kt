package com.d104.data.utils

import android.util.Base64
import com.d104.domain.utils.Base64Encoder
import javax.inject.Inject

class AndroidBase64Encoder @Inject constructor() : Base64Encoder {
    override fun encodeToString(input: ByteArray): String {
        // ImageSenderService에서 사용한 것과 동일한 플래그 사용 (예: NO_WRAP)
        return Base64.encodeToString(input, Base64.NO_WRAP)
    }
}