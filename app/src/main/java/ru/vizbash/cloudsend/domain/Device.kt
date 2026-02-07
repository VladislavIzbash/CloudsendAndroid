package ru.vizbash.cloudsend.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Device(
    val name: String,
    val uuid: Uuid,
    val isOnline: Boolean,
)