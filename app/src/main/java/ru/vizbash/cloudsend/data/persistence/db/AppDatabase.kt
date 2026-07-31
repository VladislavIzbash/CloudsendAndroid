package ru.vizbash.cloudsend.data.persistence.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DeviceEntity::class, CompletedTransferEntity::class],
    exportSchema = false,
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

    abstract fun completedTransferDao(): CompletedTransferDao
}