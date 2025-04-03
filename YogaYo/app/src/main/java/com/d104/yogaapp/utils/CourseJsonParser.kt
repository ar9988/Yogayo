package com.d104.yogaapp.utils

import android.content.Context
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.Type
import javax.inject.Inject

class CourseJsonParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // YogaPose 클래스를 위한 커스텀 디시리얼라이저 정의
    private class YogaPoseDeserializer : JsonDeserializer<YogaPose> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): YogaPose {
            val jsonObject = json.asJsonObject

            // JSON에서 필요한 값들 추출
            val poseId = jsonObject.get("poseId").asLong
            val poseName = jsonObject.get("poseName").asString
            val poseImg = jsonObject.get("poseImg").asString
            val poseLevel = jsonObject.get("poseLevel").asInt
            val poseAnimation = jsonObject.get("poseAnimation").asString
            val setPoseId = jsonObject.get("setPoseId").asLong
            val poseVideo = jsonObject.get("poseVideo").asString

            // poseDescription을 \n 기준으로 분할하여 리스트로 변환
            val poseDescription = jsonObject.get("poseDescription").asString
            val poseDescriptions = poseDescription
                .split("\n")
                .map { it.trim() }  // 각 라인의 앞뒤 공백 제거
                .filter { it.isNotEmpty() }  // 빈 라인 제거

            // YogaPose 객체 생성하여 반환
            return YogaPose(
                poseId = poseId,
                poseName = poseName,
                poseImg = poseImg,
                poseLevel = poseLevel,
                poseDescriptions = poseDescriptions,
                poseAnimation = poseAnimation,
                setPoseId = setPoseId,
                poseVideo = poseVideo
            )
        }
    }

    // Gson 인스턴스 생성 (YogaPose 커스텀 디시리얼라이저 등록)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(YogaPose::class.java, YogaPoseDeserializer())
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