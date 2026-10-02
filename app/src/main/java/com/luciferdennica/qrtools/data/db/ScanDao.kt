package com.luciferdennica.qrtools.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<ScanEntity>>

    @Insert
    suspend fun insert(item: ScanEntity): Long

    @Update
    suspend fun update(item: ScanEntity)

    @Delete
    suspend fun delete(item: ScanEntity)

    @Query("UPDATE scans SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM scans")
    suspend fun clearAll()
}
