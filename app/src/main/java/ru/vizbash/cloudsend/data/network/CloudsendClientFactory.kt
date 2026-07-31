package ru.vizbash.cloudsend.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import ru.vizbash.cloudsend.data.persistence.TokenRepository
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

            install(HttpTimeout) {
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 10000
                requestTimeoutMillis = 10000
            }

            install(Logging) {
                level = LogLevel.HEADERS
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("HTTP", message)
                    }
                }
            }

            CloudsendClient.configureHttpClient(this, baseUrl, tokenRepository)
        }
        return CloudsendClient(httpClient)
    }
}

