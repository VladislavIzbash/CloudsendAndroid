package ru.vizbash.cloudsend.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.ktor.websocket.close
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import ru.vizbash.cloudsend.data.network.dto.AuthorizeRequest
import ru.vizbash.cloudsend.data.network.dto.DeviceResponse
import ru.vizbash.cloudsend.data.network.dto.RefreshRequest
import ru.vizbash.cloudsend.data.network.dto.RegisterRequest
import ru.vizbash.cloudsend.data.network.dto.SendMessage
import ru.vizbash.cloudsend.data.network.dto.SendRequest
import ru.vizbash.cloudsend.data.network.dto.SendResponse
import ru.vizbash.cloudsend.data.network.dto.TokensResponse
import ru.vizbash.cloudsend.data.persistence.TokenRepository
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException

private val JSON = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private val NON_AUTH_URLS = listOf(
    "/auth/authorize",
    "/auth/refresh",
)

private const val TAG = "CloudSendClient"

class CloudsendClient(
    private val httpClient: HttpClient,
) : Closeable by httpClient {
    suspend fun login(login: String, password: String): TokensResponse {
        return httpClient.post("auth/authorize") {
            setBody(AuthorizeRequest(login, password))
        }.body()
    }

    private suspend fun refreshTokens(refreshToken: String): TokensResponse {
        return httpClient.post("auth/refresh") {
            setBody(RefreshRequest(refreshToken))
        }.body()
    }

    suspend fun registerDevice(uuid: String, name: String) {
        httpClient.post("devices") {
            setBody(RegisterRequest(uuid, name))
        }
    }

    suspend fun listDevices(): List<DeviceResponse> {
        return httpClient.get("devices").body()
    }

    suspend fun requestTransfer(
        request: SendRequest,
        senderUuid: String,
        targetUuid: String,
    ): SendResponse {
        return httpClient.post("send") {
            parameter("sender_uuid", senderUuid)
            parameter("target_uuid", targetUuid)

            setBody(request)
        }.body()
    }

    suspend fun logout() {
        httpClient.post("auth/logout")
    }

    suspend fun downloadFile(
        transferUuid: String,
        onDownloadProgress: (Long) -> Unit,
    ): InputStream {
        return httpClient.get("transfer/download") {
            parameter("transfer_uuid", transferUuid)

            onDownload { downloaded, _ ->
                onDownloadProgress(downloaded)
            }
        }.bodyAsChannel().toInputStream()
    }

    suspend fun uploadFile(
        transferUuid: String,
        inputStream: InputStream,
        onUploadProgress: (Long) -> Unit,
    ) {
        httpClient.post("transfer/upload") {
            parameter("transfer_uuid", transferUuid)

            contentType(ContentType.Application.OctetStream)
            setBody(inputStream.toByteReadChannel())

            onUpload { uploaded, _ ->
                onUploadProgress(uploaded)
            }
        }
    }

    suspend fun rejectTransfer(transferUuid: String) {
        httpClient.post("transfer/reject") {
            parameter("transfer_uuid", transferUuid)
        }
    }

    suspend fun listenIncomingTransfers(deviceUuid: String): Flow<SendMessage> {
        return channelFlow {
            httpClient.webSocket(
                urlString = "listen",
                request = {
                    parameter("device_uuid", deviceUuid)
                }
            ) {
                try {
                    val message = receiveDeserialized<SendMessage>()
                    this@channelFlow.send(message)
                } catch (e: CancellationException) {
                    this.close()
                }
            }
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
                contentType(ContentType.Application.Json)

                if (baseUrl.endsWith('/')) {
                    url(baseUrl)
                } else {
                    url(baseUrl + "/")
                }
            }

            install(ContentNegotiation) {
                json(JSON)
            }

            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
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
                        NON_AUTH_URLS.none {
                            request.url.encodedPath.startsWith(it)
                        }
                    }
                }
            }
        }
    }
}
