package com.d104.domain.utils

interface Base64Encoder {
    /**
     * 입력 ByteArray를 Base64 문자열로 인코딩합니다.
     * @param input 인코딩할 ByteArray
     * @return Base64로 인코딩된 문자열
     */
    fun encodeToString(input: ByteArray): String
}