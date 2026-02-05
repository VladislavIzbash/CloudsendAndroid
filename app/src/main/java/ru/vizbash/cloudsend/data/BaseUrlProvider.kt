package ru.vizbash.cloudsend.data

interface BaseUrlRepository {
    fun getBaseUrl(): String

    fun saveBaseUrl(baseUrl: String)
}
