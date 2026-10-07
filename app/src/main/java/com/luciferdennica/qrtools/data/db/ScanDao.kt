package com.luciferdennica.qrtools.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    @Query("SELECT * FROM scans WHERE isHidden = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE isHidden = 0 ORDER BY isPinned DESC, timestamp DESC")
    suspend fun getAllOnce(): List<ScanEntity>

    @Query("SELECT * FROM scans WHERE isFavorite = 1 AND isHidden = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getFavorites(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE isHidden = 0 AND content LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun search(query: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE isHidden = 0 AND type = :type ORDER BY isPinned DESC, timestamp DESC")
    fun getByType(type: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE isHidden = 0 AND type = :type AND content LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchByType(type: String, query: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScanEntity?

    @Insert
    suspend fun insert(item: ScanEntity): Long

    @Update
    suspend fun update(item: ScanEntity)

    @Delete
    suspend fun delete(item: ScanEntity)

    @Query("DELETE FROM scans WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("UPDATE scans SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE scans SET isPinned = :pin WHERE id = :id")
    suspend fun setPinned(id: Long, pin: Boolean)

    @Query("UPDATE scans SET note = :note WHERE id = :id")
    suspend fun setNote(id: Long, note: String)

    @Query("DELETE FROM scans WHERE isHidden = 1")
    suspend fun clearHidden()

    @Query("DELETE FROM scans")
    suspend fun clearAll()
}
