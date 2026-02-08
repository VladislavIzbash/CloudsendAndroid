package ru.vizbash.cloudsend.domain

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val name: String,
    val uuid: String,
    val isOnline: Boolean,
)