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
    fun getByType(type: ScanType): Flow<List<ScanEntity>> = dao.getByType(type.name)
    fun searchByType(type: ScanType, query: String): Flow<List<ScanEntity>> =
        dao.searchByType(type.name, query)
    suspend fun getById(id: Long): ScanEntity? = dao.getById(id)

    /**
     * Добавляет скан в историю.
     * @param saveToHistory — если false, запись помечается isHidden и не показывается в списке
     */
    suspend fun add(
        content: String,
        format: String,
        type: ScanType,
        saveToHistory: Boolean = true
    ): Long {
        return dao.insert(
            ScanEntity(
                content = content,
                format = format,
                type = type.name,
                isHidden = !saveToHistory
            )
        )
    }

    suspend fun toggleFavorite(item: ScanEntity) {
        dao.setFavorite(item.id, !item.isFavorite)
    }

    suspend fun setFavorite(id: Long, fav: Boolean) {
        dao.setFavorite(id, fav)
    }

    suspend fun togglePinned(item: ScanEntity) {
        dao.setPinned(item.id, !item.isPinned)
    }

    suspend fun setPinned(id: Long, pinned: Boolean) {
        dao.setPinned(id, pinned)
    }

    suspend fun setNote(id: Long, note: String) {
        dao.setNote(id, note)
    }

    suspend fun delete(item: ScanEntity) = dao.delete(item)

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun clearAll() = dao.clearAll()
}
