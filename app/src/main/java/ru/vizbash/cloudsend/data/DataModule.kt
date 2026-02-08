package ru.vizbash.cloudsend.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.vizbash.cloudsend.data.network.CloudsendClient
import ru.vizbash.cloudsend.data.network.CloudsendClientFactory
import ru.vizbash.cloudsend.data.persistence.ConnectionParamsRepository
import ru.vizbash.cloudsend.data.persistence.PreferencesStorage
import ru.vizbash.cloudsend.data.persistence.TokenRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindBaseUrlRepository(preferencesStorage: PreferencesStorage): ConnectionParamsRepository

    @Binds
    abstract fun bindTokenRepository(preferencesStorage: PreferencesStorage): TokenRepository

    companion object {
        @Provides
        fun provideCloudsendClient(
            cloudsendClientFactory: CloudsendClientFactory,
            connectionParamsRepository: ConnectionParamsRepository,
        ): CloudsendClient {
            return cloudsendClientFactory.create(connectionParamsRepository.get().baseUrl)
        }
    }
}
