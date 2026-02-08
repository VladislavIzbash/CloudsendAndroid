package ru.vizbash.cloudsend.data.persistence.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeviceEntity(
    @PrimaryKey val uuid: String,
)
