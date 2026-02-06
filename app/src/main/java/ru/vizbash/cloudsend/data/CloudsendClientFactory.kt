package ru.vizbash.cloudsend.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
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

            install(Logging) {
                level = LogLevel.ALL
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

