package com.d104.data.di

import com.d104.data.repository.AuthRepositoryImpl
import com.d104.data.repository.LobbyRepositoryImpl
import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.LobbyRepository
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
}