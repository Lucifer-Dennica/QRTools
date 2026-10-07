package com.luciferdennica.qrtools.data.repo

import com.luciferdennica.qrtools.data.db.ScanDao
import com.luciferdennica.qrtools.data.db.ScanEntity
import com.luciferdennica.qrtools.domain.model.ScanType
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: ScanDao) {

    fun getAll(): Flow<List<ScanEntity>> = dao.getAll()
    suspend fun getAllOnce(): List<ScanEntity> = dao.getAllOnce()
    fun getFavorites(): Flow<List<ScanEntity>> = dao.getFavorites()
    fun search(query: String): Flow<List<ScanEntity>> = dao.search(query)
    suspend fun getById(id: Long): ScanEntity? = dao.getById(id)

    suspend fun add(content: String, format: String, type: ScanType): Long {
        return dao.insert(ScanEntity(content = content, format = format, type = type.name))
    }

    suspend fun toggleFavorite(item: ScanEntity) {
        dao.setFavorite(item.id, !item.isFavorite)
    }

    suspend fun setFavorite(id: Long, fav: Boolean) {
        dao.setFavorite(id, fav)
    }

    suspend fun setNote(id: Long, note: String) {
        dao.setNote(id, note)
    }

    suspend fun delete(item: ScanEntity) = dao.delete(item)

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun clearAll() = dao.clearAll()
}
