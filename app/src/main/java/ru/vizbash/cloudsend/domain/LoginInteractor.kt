package ru.vizbash.cloudsend.domain

import android.util.Log
import ru.vizbash.cloudsend.data.network.CloudsendClientFactory
import ru.vizbash.cloudsend.data.persistence.ConnectionParams
import ru.vizbash.cloudsend.data.persistence.ConnectionParamsRepository
import ru.vizbash.cloudsend.data.persistence.TokenRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "LoginInteractor"

@OptIn(ExperimentalUuidApi::class)
class LoginInteractor @Inject constructor(
    private val connectionParamsRepository: ConnectionParamsRepository,
    private val tokenRepository: TokenRepository,
    private val cloudsendClientFactory: CloudsendClientFactory,
) {
    suspend operator fun invoke(
        baseUrl: String,
        login: String,
        password: String,
        deviceName: String,
    ): Boolean {
        val client = cloudsendClientFactory.create(baseUrl)

        return try {
            val tokens = client.login(login, password)
            Log.i(TAG, "Authenticated to $baseUrl")

            tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken)

            val deviceUuid = Uuid.random().toString()
            client.registerDevice(deviceUuid, deviceName)
            Log.i(TAG, "Registered self as $deviceName with uuid=$deviceUuid")

            connectionParamsRepository.save(ConnectionParams(baseUrl, deviceUuid))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to login to server: ${e.message}")
            false
        } finally {
            client.close()
        }
    }
}