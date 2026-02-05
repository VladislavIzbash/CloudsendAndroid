package ru.vizbash.cloudsend.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.logging.Logging
import javax.inject.Inject

class CloudsendClientFactory @Inject constructor(
    private val tokenRepository: TokenRepository,
) {
    fun create(baseUrl: String): CloudsendClient {
        val httpClient = HttpClient(OkHttp) {
            install(ContentEncoding) {
                deflate(1.0F)
                gzip(0.9F)
            }

            install(HttpTimeout)

            install(Logging)

            CloudsendClient.configureHttpClient(this, baseUrl, tokenRepository)
        }
        return CloudsendClient(httpClient)
    }
}

