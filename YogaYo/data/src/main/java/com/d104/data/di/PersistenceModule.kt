package com.d104.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.d104.data.local.dao.DataStorePreferencesDao
import com.d104.data.repository.DataStoreRepositoryImpl
import com.d104.domain.repository.DataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    private const val DATA_STORE_NAME = "data_store"

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile(DATA_STORE_NAME)
            }
        )
    }

    @Provides
    @Singleton
    fun provideDataStoreRepository(dataStoreDao: DataStorePreferencesDao): DataStoreRepository {
        return DataStoreRepositoryImpl(dataStoreDao)
    }
}
