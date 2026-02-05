package ru.vizbash.cloudsend.domain

import android.util.Log
import dagger.Lazy
import ru.vizbash.cloudsend.data.BaseUrlRepository
import ru.vizbash.cloudsend.data.CloudsendClient
import ru.vizbash.cloudsend.data.CloudsendClientFactory
import ru.vizbash.cloudsend.data.TokenRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "LoginInteractor"

@OptIn(ExperimentalUuidApi::class)
class LoginInteractor @Inject constructor(
    private val baseUrlRepository: BaseUrlRepository,
    private val tokenRepository: TokenRepository,
    private val cloudsendClientFactory: CloudsendClientFactory,
) {
    suspend operator fun invoke(
        baseUrl: String,
        login: String,
        password: String,
        deviceName: String,
    ): Boolean {
        return try {
            val client = cloudsendClientFactory.create(baseUrl)

            val tokens = client.login(login, password)
            Log.i(TAG, "Authenticated to $baseUrl")

            val deviceUuid = Uuid.random()
            client.registerDevice(deviceUuid, deviceName)
            Log.i(TAG, "Registered self as $deviceName with uuid=$deviceUuid")

            baseUrlRepository.saveBaseUrl(baseUrl)
            tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken)
            true
        } catch (e: Exception) {
            baseUrlRepository.saveBaseUrl("")
            Log.e(TAG, "Failed to login to server: ${e.message}")
            false
        }
    }
}