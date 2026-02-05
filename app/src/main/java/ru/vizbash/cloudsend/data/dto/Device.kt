@file:OptIn(ExperimentalUuidApi::class)

package ru.vizbash.cloudsend.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class DeviceResponse(
    @SerialName("uuid")
    val uuid: Uuid,
    @SerialName("name")
    val name: String,
    @SerialName("available")
    val available: Boolean,
)

@Serializable
data class RegisterRequest(
    @SerialName("device_uuid")
    val deviceUuid: Uuid,
    @SerialName("device_name")
    val deviceName: String,
)
