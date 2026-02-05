package ru.vizbash.cloudsend.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.vizbash.cloudsend.data.dto.AuthorizeRequest
import ru.vizbash.cloudsend.data.dto.RefreshRequest
import ru.vizbash.cloudsend.data.dto.RegisterRequest
import ru.vizbash.cloudsend.data.dto.TokensResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val JSON = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private val NON_AUTH_URLS = listOf(
    "/auth/authorize",
    "/auth/refresh",
)

private const val TAG = "CloudSendClient"

@OptIn(ExperimentalUuidApi::class)
class CloudsendClient(
    private val httpClient: HttpClient,
) {
    suspend fun login(login: String, password: String): TokensResponse {
        return httpClient.post("auth/authorize") {
            contentType(ContentType.Application.Json)
            setBody(AuthorizeRequest(login, password))
        }.body()
    }

    private suspend fun refreshTokens(refreshToken: String): TokensResponse {
        return httpClient.post("auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body()
    }

    suspend fun registerDevice(uuid: Uuid, name: String) {
        httpClient.post("devices") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(uuid, name))
        }
    }

    companion object {
        fun configureHttpClient(
            config: HttpClientConfig<*>,
            baseUrl: String,
            tokenRepository: TokenRepository,
        ) = with(config) {
            if (baseUrl.isBlank()) {
                Log.e(TAG, "Client configured with empty base url")
            }

            expectSuccess = true

            install(DefaultRequest) {
                if (baseUrl.endsWith('/')) {
                    url(baseUrl)
                } else {
                    url(baseUrl + "/")
                }
            }

            install(ContentNegotiation) {
                json(JSON)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val (access, refresh) = tokenRepository.loadTokens()
                        access?.let {
                            BearerTokens(access, refresh)
                        }
                    }

                    refreshTokens {
                        oldTokens?.refreshToken?.let { refreshToken ->
                            val resp = CloudsendClient(client).refreshTokens(refreshToken)
                            BearerTokens(resp.accessToken, resp.refreshToken)
                        }
                    }

                    sendWithoutRequest { request ->
                        NON_AUTH_URLS.any {
                            request.url.encodedPath.startsWith(it)
                        }
                    }
                }
            }
        }
    }
}
