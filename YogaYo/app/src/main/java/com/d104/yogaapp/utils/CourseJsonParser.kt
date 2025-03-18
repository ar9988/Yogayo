package com.d104.yogaapp.utils

import android.content.Context
import com.d104.domain.model.UserCourse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CourseJsonParser@Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Gson 인스턴스 생성 (Boolean 필드 커스텀 처리)
    private val gson: Gson = GsonBuilder()
        .create()

    // assets에서 JSON 파일 읽어서 UserCourse 리스트로 변환
    fun loadUserCoursesFromAssets(fileName: String): List<UserCourse> {
        try {
            // assets에서 파일 읽기
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }

            // JSON 문자열을 List<UserCourse>로 변환
            val courseListType = object : TypeToken<List<UserCourse>>() {}.type
            return gson.fromJson(jsonString, courseListType)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}