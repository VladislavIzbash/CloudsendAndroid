package ru.vizbash.cloudsend.data

interface TokenRepository {
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    suspend fun loadTokens(): Pair<String?, String?>
}
