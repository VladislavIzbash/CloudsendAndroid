package ru.vizbash.cloudsend.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.uuid.Uuid

private const val PREF_NAME = "data"
private const val KEY_BASE_URL = "base_url"
private const val KEY_DEVICE_UUID = "device_uuid"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"

class PreferencesStorage @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : ConnectionParamsRepository, TokenRepository {
    private val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override val isInitialized: Boolean
        get() = sharedPrefs.contains(KEY_BASE_URL)

    override fun save(params: ConnectionParams) {
        sharedPrefs.edit {
            putString(KEY_BASE_URL, params.baseUrl)
            putString(KEY_DEVICE_UUID, params.deviceUuid.toString())
        }
    }

    override fun get(): ConnectionParams {
        val baseUrl = sharedPrefs.getString(KEY_BASE_URL, null)
            ?: throw RuntimeException("Base url is not initialized")
        val deviceUuid = sharedPrefs.getString(KEY_DEVICE_UUID, null)
            ?: throw RuntimeException("Base url is not initialized")

        return ConnectionParams(baseUrl, Uuid.parse(deviceUuid))
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPrefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    override suspend fun loadTokens(): Pair<String?, String?> {
        return Pair(
            sharedPrefs.getString(KEY_ACCESS_TOKEN, null),
            sharedPrefs.getString(KEY_REFRESH_TOKEN, null),
        )
    }
}