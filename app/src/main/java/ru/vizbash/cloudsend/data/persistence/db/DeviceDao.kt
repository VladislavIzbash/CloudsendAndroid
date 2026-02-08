package ru.vizbash.cloudsend.data.persistence.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM device WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): DeviceEntity

    @Query("DELETE FROM device")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Transaction
    suspend fun updateAll(devices: List<DeviceEntity>) {
        clear()
        insertAll(devices)
    }
}