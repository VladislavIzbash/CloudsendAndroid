package ru.vizbash.cloudsend.data

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.vizbash.cloudsend.data.network.CloudsendClient
import ru.vizbash.cloudsend.data.network.CloudsendClientFactory
import ru.vizbash.cloudsend.data.persistence.ConnectionParamsRepository
import ru.vizbash.cloudsend.data.persistence.PreferencesStorage
import ru.vizbash.cloudsend.data.persistence.TokenRepository
import ru.vizbash.cloudsend.data.persistence.db.AppDatabase
import ru.vizbash.cloudsend.data.persistence.db.CompletedTransferDao
import ru.vizbash.cloudsend.data.persistence.db.DeviceDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindBaseUrlRepository(preferencesStorage: PreferencesStorage): ConnectionParamsRepository

    @Binds
    abstract fun bindTokenRepository(preferencesStorage: PreferencesStorage): TokenRepository

    companion object {

        @Singleton
        @Provides
        fun provideCloudsendClient(
            cloudsendClientFactory: CloudsendClientFactory,
            connectionParamsRepository: ConnectionParamsRepository,
        ): CloudsendClient {
            return cloudsendClientFactory.create(connectionParamsRepository.get().baseUrl)
        }

        @Singleton
        @Provides
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "database",
            ).build()
        }

        @Provides
        fun provideDeviceDao(database: AppDatabase): DeviceDao = database.deviceDao()

        @Provides
        fun provideCompletedTransferDao(database: AppDatabase): CompletedTransferDao =
            database.completedTransferDao()
    }
}
