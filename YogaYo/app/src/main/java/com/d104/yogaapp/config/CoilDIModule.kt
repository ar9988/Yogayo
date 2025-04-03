package com.d104.yogaapp.config

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoilDIModule {
    @Provides
    @Singleton // 앱 전체에서 하나의 ImageLoader 인스턴스만 사용하도록 싱글톤으로 제공
    fun provideImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 사용 가능한 메모리의 25% 사용
                    .build()
            }
            .diskCache { // 디스크 캐시 설정 (선택 사항)
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache")) // 캐시 디렉토리
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false)
            .apply {
            }
            .crossfade(true) // 이미지 로드 시 Crossfade 애니메이션 효과 (권장)
            .build()
    }
}