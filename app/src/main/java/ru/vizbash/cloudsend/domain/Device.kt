package ru.vizbash.cloudsend.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Device(
    val name: String,
    val uuid: Uuid,
    val isOnline: Boolean,
)