package com.d104.data.di

import com.d104.data.local.dao.DataStorePreferencesDao
import com.d104.data.local.dao.PreferencesDao
import com.d104.data.repository.AuthRepositoryImpl
import com.d104.data.repository.ImageReassemblyRepositoryImpl
import com.d104.data.repository.ImageSenderRepositoryImpl
import com.d104.data.repository.LobbyRepositoryImpl
import com.d104.data.repository.UserCourseRepositoryImpl
import com.d104.data.repository.UserRepositoryImpl
import com.d104.data.repository.WebRTCRepositoryImpl
import com.d104.data.repository.YogaPoseHistoryRepositoryImpl
import com.d104.data.repository.WebSocketRepositoryImpl
import com.d104.data.repository.YogaPoseRepositoryImpl
import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.ImageReassemblyRepository
import com.d104.domain.repository.ImageSenderRepository
import com.d104.domain.repository.LobbyRepository
import com.d104.domain.repository.UserCourseRepository
import com.d104.domain.repository.UserRepository
import com.d104.domain.repository.WebRTCRepository
import com.d104.domain.repository.YogaPoseHistoryRepository
import com.d104.domain.repository.WebSocketRepository
import com.d104.domain.repository.YogaPoseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLobbyRepository(impl: LobbyRepositoryImpl): LobbyRepository

    @Binds
    @Singleton
    abstract fun bindYogaPoseRepository(impl:YogaPoseRepositoryImpl):YogaPoseRepository

    @Binds
    @Singleton
    abstract fun bindUserCourseRepository(impl: UserCourseRepositoryImpl): UserCourseRepository

    @Binds
    @Singleton
    abstract fun bindWebSocketRepository(impl: WebSocketRepositoryImpl): WebSocketRepository

    @Binds
    @Singleton
    abstract fun bindYogaPoseHistoryRepositoryRepository(impl: YogaPoseHistoryRepositoryImpl): YogaPoseHistoryRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindWebRTCRepository(impl: WebRTCRepositoryImpl): WebRTCRepository

    @Binds
    @Singleton
    abstract fun bindImageSenderRepository(
        imageSenderRepositoryImpl: ImageSenderRepositoryImpl
    ): ImageSenderRepository

    @Binds
    @Singleton
    abstract fun bindImageReassemblyRepository(impl: ImageReassemblyRepositoryImpl): ImageReassemblyRepository

    @Binds
    @Singleton // DataStorePreferencesDao가 @Singleton이므로 동일하게 적용
    abstract fun bindPreferencesDao(
        dataStorePreferencesDaoImpl: DataStorePreferencesDao // 구현체 주입 요청
    ): PreferencesDao // 인터페이스 반환 타입
}