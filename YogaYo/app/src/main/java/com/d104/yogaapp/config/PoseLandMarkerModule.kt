package com.d104.yogaapp.config

import android.content.Context
import com.d104.yogaapp.utils.PoseLandmarkerHelper // 경로 확인
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent // ViewModel 범위
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class) // ViewModel 생명주기에 맞춰 Helper 인스턴스 관리
object PoseLandmarkerModule {

    @Provides
    @ViewModelScoped // ViewModel 인스턴스당 하나의 Helper 인스턴스 보장
    fun providePoseLandmarkerHelper(
        @ApplicationContext context: Context
    ): PoseLandmarkerHelper {
        // Helper 생성 시 필요한 기본 설정 여기서 지정 가능
        return PoseLandmarkerHelper(
            context = context,
            // 필요 시 초기 confidence 값 등 설정 가능
            // minPoseDetectionConfidence = 0.6f
        )
    }
}