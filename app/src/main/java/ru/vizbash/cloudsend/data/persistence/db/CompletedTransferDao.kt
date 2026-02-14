package ru.vizbash.cloudsend.data.persistence.db

import androidx.room.Query
import kotlinx.coroutines.flow.Flow

interface CompletedTransferDao {
    @Query("SELECT * FROM completed_transfer")
    suspend fun getAll(): Flow<List<CompletedTransferEntity>>

    @Query("SELECT * FROM completed_transfer WHERE id = :id")
    suspend fun getById(id: Int): CompletedTransferEntity

    @Query("DELETE FROM completed_transfer WHERE id = :id")
    suspend fun deleteById(id: Int)
}