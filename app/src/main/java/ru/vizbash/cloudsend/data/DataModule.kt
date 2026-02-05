package ru.vizbash.cloudsend.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindBaseUrlRepository(preferencesStorage: PreferencesStorage): BaseUrlRepository

    @Binds
    abstract fun bindTokenRepository(preferencesStorage: PreferencesStorage): TokenRepository

    companion object {
        @Provides
        fun provideCloudsendClient(
            cloudsendClientFactory: CloudsendClientFactory,
            baseUrlRepository: BaseUrlRepository,
        ): CloudsendClient {
            return cloudsendClientFactory.create(baseUrlRepository.getBaseUrl())
        }
    }
}
