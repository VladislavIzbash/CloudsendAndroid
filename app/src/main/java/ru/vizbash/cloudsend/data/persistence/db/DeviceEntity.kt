package ru.vizbash.cloudsend.data.persistence.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("device")
data class DeviceEntity(
    @PrimaryKey
    @ColumnInfo("uuid")
    val uuid: String,
    @ColumnInfo("name")
    val name: String,
)
