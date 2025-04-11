package com.d104.domain.utils

interface ImageCompressor {
    fun compress(input: ByteArray, quality: Int): ByteArray
}