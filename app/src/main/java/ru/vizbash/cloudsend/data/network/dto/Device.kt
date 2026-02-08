package ru.vizbash.cloudsend.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceResponse(
    @SerialName("uuid")
    val uuid: String,
    @SerialName("name")
    val name: String,
    @SerialName("available")
    val available: Boolean,
)

@Serializable
data class RegisterRequest(
    @SerialName("device_uuid")
    val deviceUuid: String,
    @SerialName("device_name")
    val deviceName: String,
)
