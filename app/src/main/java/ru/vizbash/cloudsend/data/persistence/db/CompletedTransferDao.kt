package ru.vizbash.cloudsend.data.persistence.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedTransferDao {
    @Query("SELECT * FROM completed_transfer ORDER BY timestamp DESC")
    fun getAllSortedByTime(): Flow<List<CompletedTransferEntity>>

    @Query("SELECT * FROM completed_transfer WHERE id = :id")
    suspend fun getById(id: Int): CompletedTransferEntity

    @Query("DELETE FROM completed_transfer WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Insert
    suspend fun insert(transfer: CompletedTransferEntity)
}