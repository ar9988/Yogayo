package com.d104.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.d104.data.local.dao.DataStorePreferencesDao
import com.d104.data.repository.DataStoreRepositoryImpl
import com.d104.domain.model.AnswerMessage
import com.d104.domain.model.IceCandidateMessage
import com.d104.domain.model.OfferMessage
import com.d104.domain.model.RoomPeersMessage
import com.d104.domain.model.SignalingMessage
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.model.UserLeftMessage
import com.d104.domain.repository.DataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import javax.inject.Singleton
import kotlinx.serialization.modules.polymorphic // polymorphic 함수 import
import kotlinx.serialization.modules.subclass // !!! subclass 함수 import 추가 !

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

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true // 알 수 없는 키 무시
        isLenient = true // 약간 유연한 파싱 허용 (필요시)
        prettyPrint = false // 필요시 true 로 변경하여 로그 가독성 높임
        encodeDefaults = true // 기본값도 JSON 에 포함 (필요시)

        // !!! 다형성 처리 설정 !!!
        serializersModule = SerializersModule {
            polymorphic(SignalingMessage::class) {
                // 각 하위 클래스를 등록
                subclass(OfferMessage::class)
                subclass(AnswerMessage::class)
                subclass(IceCandidateMessage::class)
                subclass(UserJoinedMessage::class) // 사용하는 모든 하위 클래스 등록
                subclass(UserLeftMessage::class)
                subclass(RoomPeersMessage::class)
                // ... 기타 SignalingMessage 하위 클래스들 ...
            }
            // 다른 sealed class 나 interface 에 대한 다형성 설정도 필요하다면 추가
        }
        // classDiscriminator = "type" // JSON 에 타입 식별자 필드 이름 지정 (선택 사항, 기본값은 "type")
        // @SerialName 사용 시에는 필요 없을 수 있음
    }
}
