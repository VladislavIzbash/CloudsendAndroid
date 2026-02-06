package ru.vizbash.cloudsend.data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ConnectionParams(
    val baseUrl: String,
    val deviceUuid: Uuid,
)
