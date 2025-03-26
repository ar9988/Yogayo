package com.d104.data.di

import com.d104.data.remote.api.AuthApiService
import com.d104.data.remote.api.SseApiService
import com.d104.data.remote.api.SseApiServiceImpl
import com.d104.data.remote.api.UserApiService
import com.d104.data.remote.api.UserCourseApiService
import com.d104.data.remote.api.YogaPoseApiService
import com.d104.data.utils.JwtInterceptor
import com.d104.data.utils.ZonedDateTimeJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://j12d104.p.ssafy.io/"

    @Provides
    @Singleton
    fun provideMoshiConverterFactory(): MoshiConverterFactory {
        val moshi = Moshi.Builder()
            .add(ZonedDateTimeJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(jwtInterceptor: JwtInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(jwtInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthNotRequired")
    fun provideNonAuthOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthNotRequired")
    fun provideNonAuthRetrofit(@Named("AuthNotRequired") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(provideMoshiConverterFactory())
            .build()
    }

    @Provides
    @Singleton
    @Named("YogaYo")
    fun provideYogaYoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(provideMoshiConverterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApiService(@Named("YogaYo") retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("AuthNotRequired") retrofit: Retrofit): AuthApiService{
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSseApiService(okHttpClient: OkHttpClient): SseApiService {
        return SseApiServiceImpl(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideYogaPoseApiService(@Named("YogaYo") retrofit: Retrofit): YogaPoseApiService {
        return retrofit.create(YogaPoseApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserCourseApiService(@Named("YogaYo") retrofit: Retrofit): UserCourseApiService {
        return retrofit.create(UserCourseApiService::class.java)
    }

}