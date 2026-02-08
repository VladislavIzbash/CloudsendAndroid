package ru.vizbash.cloudsend.data.persistence

import kotlin.uuid.Uuid

data class ConnectionParams(
    val baseUrl: String,
    val deviceUuid: Uuid,
)
