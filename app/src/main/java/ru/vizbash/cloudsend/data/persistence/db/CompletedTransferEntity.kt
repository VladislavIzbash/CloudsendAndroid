package ru.vizbash.cloudsend.data.persistence.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("completed_transfer")
data class CompletedTransferEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("transfer_uuid")
    val transferUuid: String,
    @ColumnInfo("filename")
    val filename: String,
    @ColumnInfo("timestamp")
    val timestamp: Long,
    @ColumnInfo("file_uri")
    val fileUri: String?,
)
