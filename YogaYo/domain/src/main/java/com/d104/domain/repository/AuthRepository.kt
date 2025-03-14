package com.d104.domain.repository

interface AuthRepository {
    fun refreshAccessToken(refreshToken: String): String
}