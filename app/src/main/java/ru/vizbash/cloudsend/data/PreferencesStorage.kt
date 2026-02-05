package ru.vizbash.cloudsend.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREF_NAME = "data"
private const val KEY_BASE_URL = "base_url"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"

class PreferencesStorage @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : BaseUrlRepository, TokenRepository {
    private val sharedPrefs = context.getSharedPreferences("data", Context.MODE_PRIVATE)

    override fun getBaseUrl(): String {
        return sharedPrefs.getString(KEY_BASE_URL, "")!!
    }

    override fun saveBaseUrl(baseUrl: String) {
        sharedPrefs.edit {
            putString(KEY_BASE_URL, baseUrl)
        }
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