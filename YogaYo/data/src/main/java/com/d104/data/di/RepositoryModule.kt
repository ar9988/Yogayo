package com.d104.data.di

import com.d104.data.repository.AuthRepositoryImpl
import com.d104.data.repository.LobbyRepositoryImpl
import com.d104.data.repository.UserCourseRepositoryImpl
import com.d104.data.repository.YogaPoseRepositoryImpl
import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.LobbyRepository
import com.d104.domain.repository.UserCourseRepository
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
}